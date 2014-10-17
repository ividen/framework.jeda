package ru.kwanza.jeda.clusterservice.impl.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Alexander Guzanov
 */
public class DBClusterService implements IClusterService, ApplicationListener<ContextRefreshedEvent> {
    public static final String NODE_ID_PROPERTY_NAME = "clusterservice.nodeId";
    public static final String SUPERVISOR_NAME = "DBClusterService-Supervisor";
    public static final String REPAIR_WORKER = "DBClusterService-RepairWorker";

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

    private long failoverTimeout = 60 * 1000;
    private long lockTimeout = 5000;
    private long repairInterval = 1000;
    private int repairThreadCount = 10;

    private ExecutorService repairExecutor;
    private AtomicLong counter = new AtomicLong(0);
    private volatile boolean started = false;
    private Supervisor supervisor;
    private ConcurrentMap<String, IClusteredModule> modules = new ConcurrentHashMap<String, IClusteredModule>();

    private volatile boolean startCritical = false;
    private ReentrantLock criticalLock = new ReentrantLock();
    private Condition isStartCritical = criticalLock.newCondition();

    @PostConstruct
    public void init() {
        initQuery();
        initCurrentNode();
        initSupervisors();
    }

    @PreDestroy
    public void destroy() {
        logger.info("Stopping {} ...", SUPERVISOR_NAME);
        supervisor.interrupt();
    }

    private void initQuery() {
        queryActive = em.queryBuilder(NodeEntity.class).where(If.isGreater("lastActivity")).create();
        queryPassive = em.queryBuilder(NodeEntity.class).where(If.isLessOrEqual("lastActivity")).create();
        queryAll = em.queryBuilder(NodeEntity.class).create();
        queryModules = em.queryBuilder(ModuleEntity.class).where(If.in("id", "id")).create();
        queryRepairableNodes = em.queryBuilder(NodeEntity.class)
                .where(If.and(If.notEqual("id", "nodeId"), If.isLessOrEqual("lastActivity", "lastActivity"))).create();
    }

    private void initCurrentNode() {
        currentNode = new NodeEntity(Integer.valueOf(System.getProperty(NODE_ID_PROPERTY_NAME)), System.currentTimeMillis());

        if (em.readByKey(NodeEntity.class, currentNode.getId()) == null) {
            try {
                em.create(currentNode);
            } catch (UpdateException e) {
                if (em.readByKey(NodeEntity.class, currentNode.getId()) == null) {
                    throw new IllegalStateException("Can't register node in database!");
                }
            }
        }
    }

    private void initSupervisors() {
        supervisor = new Supervisor();

        repairExecutor = new ThreadPoolExecutor(repairThreadCount, repairThreadCount, lockTimeout, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new Thread(r, REPAIR_WORKER + "-" + counter.incrementAndGet());
            }
        });
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

    private List<ModuleEntity> selectModuleEntities(int nodeId) {
        return queryModules.prepare().setParameter("id", ModuleEntity.getIds(nodeId, modules.values())).selectList();
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    public <R> R criticalSection(Callable<R> callable) throws InterruptedException, InvocationTargetException {
        while (!startCritical) {
            criticalLock.lock();
            try {
                isStartCritical.await();
            } finally {
                criticalLock.unlock();
            }
        }
        try {
            return callable.call();
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }

    public <R> R criticalSection(Callable<R> callable, long waiteTimeout, TimeUnit unit)
            throws InterruptedException, InvocationTargetException, TimeoutException {
        while (!startCritical) {
            criticalLock.lock();
            try {
                if (!isStartCritical.await(waiteTimeout, unit)) {
                    throw new TimeoutException();
                }
            } finally {
                criticalLock.unlock();
            }
        }
        try {
            return callable.call();
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }

    public void registerModule(IClusteredModule module) {
        if (started) {
            throw new IllegalStateException("Can't regiter module " + module.getName() +
                    "! Supervisor " + SUPERVISOR_NAME + "is already started!");
        }

        if (em.readByKey(ModuleEntity.class, ModuleEntity.createId(currentNode.getId(), module.getName())) == null) {
            try {
                em.create(new ModuleEntity(currentNode.getId(), module.getName()));
            } catch (UpdateException e) {
                logger.debug("Module {} already registered in database for node {}", module.getName(), currentNode.getId());
            }
        }

        modules.put(module.getName(), module);
    }

    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.info("Starting {} ...", SUPERVISOR_NAME);
        supervisor.start();
        started = true;
    }

    public class Supervisor extends Thread {
        private boolean isAlive = false;
        private List<ModuleEntity> moduleEntities;
        private ConcurrentMap<Integer, ConcurrentMap<RepairWorker, ModuleEntity>> repairingNodes
                = new ConcurrentHashMap<Integer, ConcurrentMap<RepairWorker, ModuleEntity>>();

        public Supervisor() {
            super(SUPERVISOR_NAME);
            this.setDaemon(true);
        }

        @Override
        public void run() {
            logger.info("Started {}", SUPERVISOR_NAME);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (!isInterrupted()) {
                long start = System.currentTimeMillis();
                try {
                    if (!tryUpdateCurrentNode(start)) continue;

                    try {
                        tm.begin();
                        if (waitForModulesLock()) {
                            startModulesIfNeed();
                            criticalSectionSignal();
                            tryRepair();
                        } else {
                            stopModulesIfNeed();
                        }

                        lockTimeout(start);
                    } finally {
                        startCritical = false;
                        tm.commit();
                    }
                } catch (InterruptedException e) {
                    break;
                }catch (Throwable e){
                    logger.error("Eror in supervisor!",e);
                    continue;
                }
            }
            logger.info("Stopped {}", SUPERVISOR_NAME);
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
            if (moduleEntities.isEmpty()) return;

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
                moduleEntities = selectModuleEntities(currentNode.getId());
                if (!moduleEntities.isEmpty()) {
                    em.lock(LockType.WAIT, ModuleEntity.class, moduleEntities);
                }
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

        private void tryRepair() {
            Map<Integer, NodeEntity> staleNodes = selectStaleActivity();

            if (!staleNodes.isEmpty()) {
                findNewRepairableNodes(staleNodes);
            }

            if (!repairingNodes.isEmpty()) {
                findReactivatedNodes(staleNodes);
            }
        }

        private void findReactivatedNodes(Map<Integer, NodeEntity> staleNodes) {
            Iterator<Map.Entry<Integer, ConcurrentMap<RepairWorker, ModuleEntity>>> i
                    = repairingNodes.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<Integer, ConcurrentMap<RepairWorker, ModuleEntity>> entry = i.next();
                if (!staleNodes.containsKey(entry.getKey())) {
                    for (RepairWorker repairWorker : entry.getValue().keySet()) {
                        repairWorker.stopWorker();
                    }
                    i.remove();
                }
            }
        }

        private void findNewRepairableNodes(Map<Integer, NodeEntity> staleNodes) {
            for (NodeEntity n : staleNodes.values()) {
                if (!repairingNodes.containsKey(n.getId())) {
                    initRepairWorkers(n);
                }
            }
        }

        private void initRepairWorkers(NodeEntity n) {
            List<ModuleEntity> moduleEntities = selectModuleEntities(n.getId());
            ConcurrentHashMap<RepairWorker, ModuleEntity> workers = new ConcurrentHashMap<RepairWorker, ModuleEntity>();
            repairingNodes.put(n.getId(), workers);
            for (ModuleEntity me : moduleEntities) {
                RepairWorker worker = new RepairWorker(n, modules.get(me.getName()), me);
                workers.put(worker, me);
                repairExecutor.submit(worker);
            }
        }

        private Map<Integer, NodeEntity> selectStaleActivity() {
            return queryRepairableNodes.prepare()
                    .setParameter("nodeId", currentNode.getId())
                    .setParameter("lastActivity", System.currentTimeMillis()).selectMap("id");
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

    private void criticalSectionSignal() {
        criticalLock.lock();
        try {
            startCritical = true;
            isStartCritical.signalAll();
        } finally {
            criticalLock.unlock();
        }
    }
}
