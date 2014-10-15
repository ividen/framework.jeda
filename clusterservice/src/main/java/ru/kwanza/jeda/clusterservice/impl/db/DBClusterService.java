package ru.kwanza.jeda.clusterservice.impl.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kwanza.dbtool.core.UpdateException;
import ru.kwanza.dbtool.orm.api.*;
import ru.kwanza.jeda.clusterservice.IClusterService;
import ru.kwanza.jeda.clusterservice.IClusteredModule;
import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.jeda.clusterservice.impl.db.orm.ModuleEntity;
import ru.kwanza.jeda.clusterservice.impl.db.orm.NodeEntity;
import ru.kwanza.txn.api.spi.ITransactionManager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Alexander Guzanov
 */
public class DBClusterService implements IClusterService {
    public static final String NODE_ID_PROPERTY_NAME = "clusterservice.nodeId";
    public static final String ACTIVITY_SUPERVISOR_NAME = "DBClusterService-ActivitySupervisor";
    public static final String REPAIR_SUPERVISOR_NAME = "DBClusterService-RepairSupervisor";
    public static final String REPAIR_SUPERVISOR_WORKER = "DBClusterService-RepairWorker";

    @Resource(name = "dbtool.IEntityManager")
    private IEntityManager em;
    @Resource(name = "txn.ITransactionManager")
    private ITransactionManager tm;

    private IQuery<NodeEntity> queryActive;
    private IQuery<NodeEntity> queryPassive;
    private IQuery<NodeEntity> queryAll;
    private IQuery<ModuleEntity> queryModules;
    private IQuery<NodeEntity> queryRepairableNodes;

    private NodeEntity currentNode;

    private static Logger logger = LoggerFactory.getLogger(DBClusterService.class);

    private long failoverTimeout = 15 * 60 * 1000;
    private long lockTimeout = 60 * 1000;
    private long repairInterval = 1000;
    private int repairThreadCount = 10;

    private ExecutorService repairExecutor;
    private AtomicLong counter = new AtomicLong(0);
    private ActivitySupervisor activitySupervisor;
    private RepairSupervisor repairSupervisor;
    private ConcurrentMap<String, IClusteredModule> modules = new ConcurrentHashMap<String, IClusteredModule>();

    private volatile boolean safe = false;
    private ReentrantLock safeLock = new ReentrantLock();
    private Condition isSafe = safeLock.newCondition();

    @PostConstruct
    public void init() {
        initQuery();
        initCurrentNode();
        initSupervisors();
    }

    @PreDestroy
    public void destroy() {
        logger.info("Stopping {} ...", ACTIVITY_SUPERVISOR_NAME);
        activitySupervisor.interrupt();
        logger.info("Stopping {} ...", REPAIR_SUPERVISOR_NAME);
        repairSupervisor.interrupt();
    }

    private void initQuery() {
        queryActive = em.queryBuilder(NodeEntity.class).where(If.isGreater("lastActivity")).create();
        queryPassive = em.queryBuilder(NodeEntity.class).where(If.isLessOrEqual("lastActivity")).create();
        queryAll = em.queryBuilder(NodeEntity.class).create();
        queryModules = em.queryBuilder(ModuleEntity.class).where(If.and(If.isEqual("nodeId", "nodeId"), If.in("name", "name"))).create();
        queryRepairableNodes = em.queryBuilder(NodeEntity.class)
                .where(If.and(If.notEqual("nodeId", "nodeId"), If.isLessOrEqual("lastActivity", "lastActivity"))).create();
    }

    private void initCurrentNode() {
        currentNode = new NodeEntity(Integer.valueOf(System.getProperty(NODE_ID_PROPERTY_NAME)), System.currentTimeMillis());

        if (em.readByKey(NodeEntity.class, currentNode.getId()) == null) {
            try {
                em.create(currentNode);
            } catch (UpdateException e) {
                if (em.readByKey(NodeEntity.class, currentNode.getId()) == null) {
                    throw new IllegalStateException("Can't register node in databse!");
                }
            }
        }
    }

    private void initSupervisors() {
        activitySupervisor = new ActivitySupervisor();
        repairSupervisor = new RepairSupervisor();

        repairExecutor = new ThreadPoolExecutor(repairThreadCount, repairThreadCount, lockTimeout, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new Thread(r, REPAIR_SUPERVISOR_WORKER + "-" + counter.incrementAndGet());
            }
        });

        logger.info("Starting {} ...", ACTIVITY_SUPERVISOR_NAME);
        activitySupervisor.start();
        logger.info("Starting {} ...", REPAIR_SUPERVISOR_NAME);
        repairSupervisor.start();
    }

    public List<? extends NodeEntity> getActiveNodes() {
        return queryActive.prepare().setParameter(1, System.currentTimeMillis()).selectList();
    }

    public List<? extends Node> getPassiveNodes() {
        return queryPassive.prepare().setParameter(1, System.currentTimeMillis()).selectList();
    }

    public List<? extends Node> getNodes() {
        return queryAll.prepare().setParameter(1, System.currentTimeMillis()).selectList();
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    //todo under construction
    public <R> R criticalSection(Callable<R> callable) throws InterruptedException, InvocationTargetException {

        while (!safe) {
            safeLock.lock();
            try {
                isSafe.await();
            } finally {
                safeLock.unlock();
            }
        }
        try {
            return callable.call();
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }

    public <R> R criticalSection(Callable<R> callable, long waiteTimeout, TimeUnit unit) {
        return null;
    }

    public void registerModule(IClusteredModule module) {
        try {
            em.create(new ModuleEntity(currentNode.getId(), module.getName()));
        } catch (UpdateException e) {
            logger.debug("Module {} already registered in database for node {}", module.getName(), currentNode.getId());
        }

        modules.put(module.getName(), module);
    }

    public final class ActivitySupervisor extends Thread {
        private boolean isAlive = false;
        private List<ModuleEntity> moduleEntities;

        public ActivitySupervisor() {
            super(ACTIVITY_SUPERVISOR_NAME);
            this.setDaemon(true);
        }

        @Override
        public void run() {
            logger.info("Started {}", ACTIVITY_SUPERVISOR_NAME);
            while (!isInterrupted()) {
                long start = System.currentTimeMillis();
                try {
                    if (!tryUpdateCurrentNode(start)) continue;

                    try {
                        tm.begin();
                        if (waitForModulesLock()) {
                            startModulesIfNeed();
                            safeLock.lock();
                            try {
                                safe = true;
                                isSafe.signalAll();
                            } finally {
                                safeLock.unlock();
                            }

                        } else {
                            stopModulesIfNeed();
                        }

                        lockTimeout(start);
                    } finally {
                        safe = false;
                        tm.commit();
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
            logger.info("Stopped {}", ACTIVITY_SUPERVISOR_NAME);
        }

        private boolean tryUpdateCurrentNode(long start) throws InterruptedException {
            if (!updateCurrentNodeActivity(start)) {
                stopModulesIfNeed();
                lockTimeout(start);
                return false;
            }
            return true;
        }

        private void startModulesIfNeed() {
            if (!isAlive) {
                isAlive = true;
                startModules();
                updateModulesLastRepaired(moduleEntities);
            }
        }

        private void stopModulesIfNeed() {
            if (isAlive) {
                isAlive = false;
                stopModules();
            }
        }

        private void lockTimeout(long start) throws InterruptedException {
            sleep(Math.max(0, lockTimeout - (System.currentTimeMillis() - start)));
        }

        private void updateModulesLastRepaired(List<ModuleEntity> moduleEntities) {
            long ts = System.currentTimeMillis();
            for (ModuleEntity moduleEntity : moduleEntities) {
                moduleEntity.setLastRepaired(ts);
            }

            try {
                em.update(ModuleEntity.class, moduleEntities);
            } catch (Throwable e) {
                logger.error("Error updating modules", e);
            }
        }

        private boolean updateCurrentNodeActivity(long ts) {
            currentNode.setLastActivity(ts + failoverTimeout);
            try {
                em.update(currentNode);
            } catch (Throwable e) {
                logger.error("Can't encrease activity timestamp for " + currentNode.getId(), e);
                return false;
            }

            return true;
        }

        private boolean waitForModulesLock() {
            try {
                moduleEntities = queryModules.prepare()
                        .setParameter("nodeId", currentNode.getId())
                        .setParameter("name", modules.keySet()).selectList();
                em.lock(LockType.WAIT, moduleEntities);
            } catch (Throwable e) {
                return false;
            }

            return true;
        }

        private void startModules() {

            for (IClusteredModule cm : modules.values()) {
                logger.info("Starting module {}", cm.getName());
                try {
                    cm.handleStart();
                } catch (Throwable e) {
                    logger.error("Can't start module " + cm.getName(), e);
                }
            }
        }

        private void stopModules() {
            for (IClusteredModule cm : modules.values()) {
                logger.info("Stopping module {}", cm.getName());
                try {
                    cm.handleStop();
                } catch (Throwable e) {
                    logger.error("Can't stop module " + cm.getName(), e);
                }
            }

        }

    }

    public final class RepairSupervisor extends Thread {
        public RepairSupervisor() {
            super(REPAIR_SUPERVISOR_NAME);
            this.setDaemon(true);
        }

        @Override
        public void run() {
            logger.info("Started {}", ACTIVITY_SUPERVISOR_NAME);
            while (!isInterrupted()) {
                List<NodeEntity> nodeEntities = queryRepairableNodes.prepare()
                        .setParameter("nodeId", currentNode.getId())
                        .setParameter("lastActivity", System.currentTimeMillis()).selectList();

                if (!nodeEntities.isEmpty()) {


                }

                try {
                    sleep(failoverTimeout);
                } catch (InterruptedException e) {
                    break;
                }
            }
            logger.info("Stopped {}", ACTIVITY_SUPERVISOR_NAME);
        }
    }


    public final class RepairWorker implements Runnable {
        private volatile boolean alive = true;
        private NodeEntity node;
        private IClusteredModule module;
        private ModuleEntity moduleEntity;

        public RepairWorker(NodeEntity node, IClusteredModule module, ModuleEntity moduleEntity) {
            this.node = node;
            this.module = module;
            this.moduleEntity = moduleEntity;
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted() && alive && !isNodeChangeStatus() && !isModuleRepaired()) {
                tm.begin();
                try {
                    if (lockModule()) {
                        if (module.handleRepair(node)) {
                            updateModuleAsRepaired();
                            alive = false;
                            break;
                        }
                    }
                } catch (Throwable e) {
                    logger.error("Error in repair thread!", e);
                } finally {
                    tm.commit();
                }

                try {
                    Thread.currentThread().sleep(repairInterval);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        private void updateModuleAsRepaired() {
            moduleEntity.setLastRepaired(System.currentTimeMillis());
            try {
                em.update(moduleEntity);
            } catch (UpdateException e) {
                e.printStackTrace();
            }
        }

        private boolean lockModule() {
            LockResult<ModuleEntity> result = em.lock(LockType.SKIP_LOCKED, moduleEntity);
            return !result.getLocked().isEmpty();
        }


        private boolean isNodeChangeStatus() {
            NodeEntity nodeEntity = em.readByKey(NodeEntity.class, node.getId());
            return nodeEntity == null || nodeEntity.getLastActivity() > node.getLastActivity();
        }

        public boolean isModuleRepaired() {
            ModuleEntity me = em.readByKey(ModuleEntity.class, moduleEntity.getId());
            return me == null || me.getLastRepaired() > moduleEntity.getLastRepaired();
        }

        public void stopWorker() {
            alive = false;
        }
    }

}
