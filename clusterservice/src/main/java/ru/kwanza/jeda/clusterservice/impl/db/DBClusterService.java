package ru.kwanza.jeda.clusterservice.impl.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import ru.kwanza.dbtool.core.DBTool;
import ru.kwanza.dbtool.core.UpdateException;
import ru.kwanza.dbtool.orm.api.IEntityManager;
import ru.kwanza.dbtool.orm.api.IQuery;
import ru.kwanza.dbtool.orm.api.If;
import ru.kwanza.jeda.clusterservice.IClusterService;
import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.jeda.clusterservice.impl.db.orm.ClusterNode;
import ru.kwanza.jeda.clusterservice.impl.db.orm.ClusteredComponent;
import ru.kwanza.jeda.clusterservice.impl.db.orm.WaitForReturnComponent;
import ru.kwanza.toolbox.fieldhelper.FieldHelper;
import ru.kwanza.txn.api.spi.ITransactionManager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Alexander Guzanov
 */
public class DBClusterService implements IClusterService, ApplicationListener<ContextRefreshedEvent> {
    public static final String SUPERVISOR_NAME = "DBClusterService-Supervisor";
    public static final String REPAIR_WORKER = "DBClusterService-RepairWorker";

    @Resource(name = "dbtool.IEntityManager")
    private IEntityManager em;
    @Resource(name = "dbtool.DBTool")
    private DBTool dbTool;
    @Resource(name = "txn.ITransactionManager")
    private ITransactionManager tm;

    private IQuery<ClusterNode> queryActive;
    private IQuery<ClusterNode> queryPassive;
    private IQuery<ClusterNode> queryAll;

    private IQuery<ClusteredComponent> queryForComponents;

    private ClusterNode currentNode;
    private Integer currentNodeId;
    private volatile long lastActivityTs;

    private static Logger logger = LoggerFactory.getLogger(DBClusterService.class);

    private long failoverInterval;
    private long activityInterval;
    private int repairThreadCount;

    private ExecutorService repairExecutor;
    private AtomicLong counter = new AtomicLong(0);
    private volatile boolean started = false;
    private Supervisor supervisor;
    private ConcurrentMap<String, IClusteredComponent> components = new ConcurrentHashMap<String, IClusteredComponent>();
    private Map<String, ClusteredComponent> activeComponents = new ConcurrentHashMap<String, ClusteredComponent>();
    private Map<String, ClusteredComponent> waitForReturnComponents = new ConcurrentHashMap<String, ClusteredComponent>();


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
        queryForComponents = em.queryBuilder(ClusteredComponent.class)
                .where(If.and(If.isEqual("nodeId"), If.in("name"))).create();
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

    public long getFailoverInterval() {
        return failoverInterval;
    }

    public void setFailoverInterval(long failoverInterval) {
        this.failoverInterval = failoverInterval;
    }

    public long getActivityInterval() {
        return activityInterval;
    }

    public void setActivityInterval(long activityInterval) {
        this.activityInterval = activityInterval;
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

        repairExecutor = new ThreadPoolExecutor(repairThreadCount, repairThreadCount, activityInterval, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
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

    public Node getCurrentNode() {
        return currentNode;
    }

    public Map<String, IClusteredComponent> getComponents() {
        return Collections.unmodifiableMap(components);
    }

    public Map<String, IClusteredComponent> getStartedComponents() {
        return null;
    }

    public Map<String, IClusteredComponent> getStoppedComponents() {
        return null;
    }

    public <R> R criticalSection(Callable<R> callable) throws InterruptedException, InvocationTargetException {
        return null;
    }

    public <R> R criticalSection(Callable<R> callable, long waitTimeout, TimeUnit unit)
            throws InterruptedException, InvocationTargetException, TimeoutException {
        return null;
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

        components.put(module.getName(), module);
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

            calcLastActivity();
            processInitialState();

            while (started && !isInterrupted()) {
                calcLastActivity();
                try {
                    try {
                        tm.begin();
                        leaseActivity();
                        checkWaitings();

                    } finally {
                        tm.commit();
                    }
                    Thread.sleep(activityInterval);
                } catch (InterruptedException e) {
                    break;
                } catch (Throwable e) {
                    logger.error("Error in supervisor!", e);
                    continue;
                }
            }

            logger.info("Stopped {}", SUPERVISOR_NAME);
        }

        private void calcLastActivity() {
            lastActivityTs = System.currentTimeMillis() + failoverInterval;
        }

        private void processInitialState() {
            List<ClusteredComponent> items = selectAllComponents();
            filterComponentByState(items);
            leaseActivity();
            startActiveComponents();
        }

        private void startActiveComponents() {
            for (ClusteredComponent item : activeComponents.values()) {
                //todo aguzanov вынести в пул потоков
                components.get(item.getName()).handleStart();
            }
        }

        private void filterComponentByState(List<ClusteredComponent> items) {
            final long ts = System.currentTimeMillis();
            for (ClusteredComponent item : items) {
                if (item.getHoldNodeId() != null) {
                    if (item.getLastActivity() <= ts) {
                        item.clearMarkers();
                        activeComponents.put(item.getName(), item);
                    } else {
                        waitForReturnComponents.put(item.getName(), item);
                    }
                } else {
                    activeComponents.put(item.getName(), item);
                }
            }
        }

        private List<ClusteredComponent> selectAllComponents() {
            return queryForComponents.prepare()
                    .setParameter(1, currentNodeId)
                    .setParameter(2, components.keySet())
                    .selectList();
        }

        private void leaseActivity() {
            try {
                updateActivity(activeComponents.values());
            } catch (UpdateException e) {
                for (ClusteredComponent o : e.<ClusteredComponent>getConstrainted()) {
                    activeComponents.remove(o.getName());
                    waitForReturnComponents.put(o.getName(), o);
                    components.get(o.getName()).handleStop();
                }
                for (ClusteredComponent o : e.<ClusteredComponent>getOptimistic()) {
                    activeComponents.remove(o.getName());
                    waitForReturnComponents.put(o.getName(), o);
                    components.get(o.getName()).handleStop();
                }
            }
        }

        private void activateCandidates(List<ClusteredComponent> activateCandidates) {
            try {
                updateActivity(activateCandidates);
            } catch (UpdateException e) {
                activateCandidates = e.getUpdated();
            }

            for (ClusteredComponent item : activateCandidates) {
                activeComponents.put(item.getName(), item);
                waitForReturnComponents.remove(item.getName());
                //todo aguzanov do in worker
                components.get(item.getName()).handleStart();
            }
        }

        private void updateActivity(Collection<ClusteredComponent> items) throws UpdateException {
            for (ClusteredComponent component : items) {
                component.setLastActivity(lastActivityTs);
                component.clearMarkers();
            }

            em.update(items);
        }

        private void checkWaitings() {
            Collection<ClusteredComponent> items = selectWaitForReturn();
            List<ClusteredComponent> activateCandidates = findCandidateForActivation(items);
            activateCandidates(activateCandidates);
            markWaitForReturn();
        }


        private void markWaitForReturn() {
            try {
                em.update(WaitForReturnComponent.class, FieldHelper.getFieldCollection(waitForReturnComponents.values(),
                        FieldHelper.<ClusteredComponent, WaitForReturnComponent>construct(ClusteredComponent.class, "waitEntity")));
            } catch (UpdateException e) {
                //todo aguzanov log error;
            }
        }

        private List<ClusteredComponent> findCandidateForActivation(Collection<ClusteredComponent> items) {
            List<ClusteredComponent> activateCandidates = new ArrayList<ClusteredComponent>();
            for (ClusteredComponent item : items) {
                if (item.getHoldNodeId() == null) {
                    activateCandidates.add(item);
                }
            }
            return activateCandidates;
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
                while (!Thread.currentThread().isInterrupted() && alive) {
                    tm.begin();
                    try {

                        tm.commit();
                    } catch (Throwable e) {
                        logger.error("Error in repair thread!", e);
                        tm.rollback();
                    }

                    try {
                        Thread.currentThread().sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }

            public void stopWorker() {
                alive = false;
            }
        }

    }

    private Collection<ClusteredComponent> selectWaitForReturn() {
        return em.readByKeys(ClusteredComponent.class,
                FieldHelper.getFieldCollection(waitForReturnComponents.values(),
                        FieldHelper.construct(ClusteredComponent.class, "id")));
    }

}
