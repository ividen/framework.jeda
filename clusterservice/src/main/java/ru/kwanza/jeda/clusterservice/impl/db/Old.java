package ru.kwanza.jeda.clusterservice.impl.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import ru.kwanza.dbtool.core.UpdateException;
import ru.kwanza.dbtool.orm.api.*;
import ru.kwanza.jeda.clusterservice.IClusterService;
import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.jeda.clusterservice.impl.db.orm.ClusteredComponent;
import ru.kwanza.jeda.clusterservice.impl.db.orm.ClusterNode;
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

/**
 * @author Alexander Guzanov
 */
public class Old implements IClusterService, ApplicationListener<ContextRefreshedEvent> {
    public static final String SUPERVISOR_NAME = "DBClusterService-Supervisor";
    public static final String REPAIR_WORKER = "DBClusterService-RepairWorker";

    @Resource(name = "dbtool.IEntityManager")
    private IEntityManager em;
    @Resource(name = "txn.ITransactionManager")
    private ITransactionManager tm;

    private IQuery<ClusterNode> queryActive;
    private IQuery<ClusterNode> queryPassive;
    private IQuery<ClusterNode> queryAll;
    private IQuery<ClusteredComponent> queryModules;
    private IQuery<ClusterNode> queryRepairableNodes;

    private ClusterNode currentNode;
    private Integer currentNodeId;

    private static Logger logger = LoggerFactory.getLogger(DBClusterService.class);

    private long failoverTimeout;
    private long lockTimeout;
    private long repairInterval;
    private int repairThreadCount;

    private ExecutorService repairExecutor;
    private AtomicLong counter = new AtomicLong(0);
    private volatile boolean started = false;
    private Supervisor supervisor;
    private ConcurrentMap<String, IClusteredComponent> modules = new ConcurrentHashMap<String, IClusteredComponent>();

    private CriticalSection criticalSection = new CriticalSection();

    @PostConstruct
    public void init() {
        initQuery();
        initCurrentNode();
        initSupervisors();
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        logger.info("Stopping {} ...", SUPERVISOR_NAME);
        started = false;
        supervisor.interrupt();
        supervisor.join(60000);
        logger.info("Stopping RepairWorker's threads ...");
        repairExecutor.shutdownNow();
        repairExecutor.awaitTermination(60000, TimeUnit.MILLISECONDS);

    }

    private void initQuery() {
        queryActive = em.queryBuilder(ClusterNode.class).where(If.isGreater("lastActivity")).create();
        queryPassive = em.queryBuilder(ClusterNode.class).where(If.isLessOrEqual("lastActivity")).create();
        queryAll = em.queryBuilder(ClusterNode.class).create();
        queryModules = em.queryBuilder(ClusteredComponent.class).where(If.in("id", "id")).create();
        queryRepairableNodes = em.queryBuilder(ClusterNode.class)
                .where(If.and(If.notEqual("id", "nodeId"), If.isLessOrEqual("lastActivity", "lastActivity"))).create();
    }

    private void initCurrentNode() {
        currentNode = new ClusterNode(currentNodeId, System.currentTimeMillis());

        if (em.readByKey(ClusterNode.class, currentNode.getId()) == null) {
            try {
                em.create(currentNode);
            } catch (UpdateException e) {
                if (em.readByKey(ClusterNode.class, currentNode.getId()) == null) {
                    throw new IllegalStateException("Can't register node in database!");
                }
            }
        }
    }

    public Integer getCurrentNodeId() {
        return currentNodeId;
    }

    public long getFailoverTimeout() {
        return failoverTimeout;
    }

    public void setFailoverTimeout(long failoverTimeout) {
        this.failoverTimeout = failoverTimeout;
    }

    public long getLockTimeout() {
        return lockTimeout;
    }

    public void setLockTimeout(long lockTimeout) {
        this.lockTimeout = lockTimeout;
    }

    public long getRepairInterval() {
        return repairInterval;
    }

    public void setRepairInterval(long repairInterval) {
        this.repairInterval = repairInterval;
    }

    public int getRepairThreadCount() {
        return repairThreadCount;
    }

    public void setRepairThreadCount(int repairThreadCount) {
        this.repairThreadCount = repairThreadCount;
    }

    public void setCurrentNodeId(Integer currentNodeId) {
        this.currentNodeId = currentNodeId;
    }

    private void initSupervisors() {
        supervisor = new Supervisor();

        repairExecutor = new ThreadPoolExecutor(repairThreadCount, repairThreadCount, lockTimeout, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new Thread(r, REPAIR_WORKER + "-" + counter.incrementAndGet());
            }
        });
    }

    public List<? extends ClusterNode> getActiveNodes() {
        return queryActive.prepare().setParameter(1, System.currentTimeMillis()).selectList();
    }

    public List<? extends Node> getPassiveNodes() {
        return queryPassive.prepare().setParameter(1, System.currentTimeMillis()).selectList();
    }

    public List<? extends Node> getNodes() {
        return queryAll.prepare().setParameter(1, System.currentTimeMillis()).selectList();
    }

    private List<ClusteredComponent> selectModuleEntities(int nodeId) {
        return queryModules.prepare().setParameter("id", ClusteredComponent.getIds(nodeId, modules.values())).selectList();
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    public <R> R criticalSection(Callable<R> callable) throws InterruptedException, InvocationTargetException {
        return criticalSection.execute(callable);
    }

    public <R> R criticalSection(Callable<R> callable, long waitTimeout, TimeUnit unit)
            throws InterruptedException, InvocationTargetException, TimeoutException {
        return criticalSection.execute(callable, waitTimeout, unit);
    }

    public void registerComponent(IClusteredComponent module) {
        if (started) {
            throw new IllegalStateException("Can't regiter module " + module.getName() +
                    "! Supervisor " + SUPERVISOR_NAME + "is already started!");
        }

        if (em.readByKey(ClusteredComponent.class, ClusteredComponent.createId(currentNode.getId(), module.getName())) == null) {
            try {
                em.create(new ClusteredComponent(currentNode.getId(), module.getName()));
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
        private List<ClusteredComponent> moduleEntities;
        private ConcurrentMap<Integer, ConcurrentMap<RepairWorker, ClusteredComponent>> repairingNodes
                = new ConcurrentHashMap<Integer, ConcurrentMap<RepairWorker, ClusteredComponent>>();

        public Supervisor() {
            super(SUPERVISOR_NAME);
            setDaemon(true);
        }

        @Override
        public void run() {
            logger.info("Started {}", SUPERVISOR_NAME);
            while (started && !isInterrupted()) {
                long start = System.currentTimeMillis();
                try {
                    if (!tryUpdateCurrentNode(start)) continue;
                    boolean isInCritical = false;
                    try {
                        tm.begin();
                        if (waitForModulesLock()) {
                            startModulesIfNeed();
                            isInCritical = true;
                            criticalSection.enter();
                            tryRepair();
                        } else {
                            stopModulesIfNeed();
                        }

                        lockTimeout(start);
                    } finally {
                        if (isInCritical) {
                            criticalSection.exit();
                        }
                        tm.commit();
                    }
                } catch (InterruptedException e) {
                    break;
                } catch (Throwable e) {
                    logger.error("Error in supervisor!", e);
                    continue;
                }
            }
            stopModulesIfNeed();
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

        private void updateModulesLastRepaired(List<ClusteredComponent> moduleEntities) {
            if (moduleEntities.isEmpty()) return;

            long ts = System.currentTimeMillis();
            for (ClusteredComponent clusteredComponent : moduleEntities) {
                clusteredComponent.setLastActivity(ts);
            }

            try {
                em.update(ClusteredComponent.class, moduleEntities);
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
                    em.lock(LockType.WAIT, ClusteredComponent.class, moduleEntities);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        private void startModules() {

            for (IClusteredComponent cm : modules.values()) {
                logger.info("Starting module {}", cm.getName());
                try {
                    cm.handleStart();
                } catch (Throwable e) {
                    logger.error("Can't start module " + cm.getName(), e);
                }
            }
        }

        private void stopModules() {
            for (IClusteredComponent cm : modules.values()) {
                logger.info("Stopping module {}", cm.getName());
                try {
                    cm.handleStop();
                } catch (Throwable e) {
                    logger.error("Can't stop module " + cm.getName(), e);
                }
            }

        }

        private void tryRepair() {
            Map<Integer, ClusterNode> staleNodes = selectStaleActivity();

            if (!staleNodes.isEmpty()) {
                findNewRepairableNodes(staleNodes);
            }

            if (!repairingNodes.isEmpty()) {
                findReactivatedNodes(staleNodes);
            }
        }

        private void findReactivatedNodes(Map<Integer, ClusterNode> staleNodes) {
            Iterator<Map.Entry<Integer, ConcurrentMap<RepairWorker, ClusteredComponent>>> i
                    = repairingNodes.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<Integer, ConcurrentMap<RepairWorker, ClusteredComponent>> entry = i.next();
                if (!staleNodes.containsKey(entry.getKey())) {
                    for (RepairWorker repairWorker : entry.getValue().keySet()) {
                        repairWorker.stopWorker();
                    }
                    i.remove();
                }
            }
        }

        private void findNewRepairableNodes(Map<Integer, ClusterNode> staleNodes) {
            for (ClusterNode n : staleNodes.values()) {
                if (!repairingNodes.containsKey(n.getId())) {
                    initRepairWorkers(n);
                }
            }
        }

        private void initRepairWorkers(ClusterNode n) {
            List<ClusteredComponent> moduleEntities = selectModuleEntities(n.getId());
            ConcurrentHashMap<RepairWorker, ClusteredComponent> workers = new ConcurrentHashMap<RepairWorker, ClusteredComponent>();
            repairingNodes.put(n.getId(), workers);
            for (ClusteredComponent me : moduleEntities) {
                RepairWorker worker = new RepairWorker(n, modules.get(me.getName()), me);
                workers.put(worker, me);
                repairExecutor.submit(worker);
            }
        }

        private Map<Integer, ClusterNode> selectStaleActivity() {
            return queryRepairableNodes.prepare()
                    .setParameter("nodeId", currentNode.getId())
                    .setParameter("lastActivity", System.currentTimeMillis()).selectMap("id");
        }

        public final class RepairWorker implements Runnable {
            private volatile boolean alive = true;
            private ClusterNode node;
            private IClusteredComponent module;
            private ClusteredComponent clusteredComponent;

            public RepairWorker(ClusterNode node, IClusteredComponent module, ClusteredComponent clusteredComponent) {
                this.node = node;
                this.module = module;
                this.clusteredComponent = clusteredComponent;
            }

            public void run() {
                while (!Thread.currentThread().isInterrupted() && alive && !isNodeChangeStatus() && !isModuleRepaired()) {
                    tm.begin();
                    try {
                        if (lockModule()) {
                            if (clusteredComponent.getLastActivity() > node.getLastActivity() || module.handleStartRepair(node)) {
                                updateModuleAsRepaired();
                                alive = false;
                                break;
                            }
                        }
                        tm.commit();
                    } catch (Throwable e) {
                        logger.error("Error in repair thread!", e);
                        tm.rollback();
                    }

                    try {
                        Thread.currentThread().sleep(repairInterval);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }

            private void updateModuleAsRepaired() {
                clusteredComponent.setLastActivity(System.currentTimeMillis());
                try {
                    em.update(clusteredComponent);
                } catch (UpdateException e) {
                    logger.error("Error updating module lastRepaired!", e);
                }
            }

            private boolean lockModule() {
                LockResult<ClusteredComponent> result = em.lock(LockType.SKIP_LOCKED, clusteredComponent);
                return !result.getLocked().isEmpty();
            }


            private boolean isNodeChangeStatus() {
                ClusterNode clusterNode = em.readByKey(ClusterNode.class, node.getId());
                return clusterNode == null || clusterNode.getLastActivity() > node.getLastActivity();
            }

            public boolean isModuleRepaired() {
                ClusteredComponent me = em.readByKey(ClusteredComponent.class, clusteredComponent.getId());
                return me == null || me.getLastActivity() > clusteredComponent.getLastActivity();
            }

            public void stopWorker() {
                alive = false;
            }
        }

    }

}
