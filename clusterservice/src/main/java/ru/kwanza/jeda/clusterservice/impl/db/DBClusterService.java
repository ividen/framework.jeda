package ru.kwanza.jeda.clusterservice.impl.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import ru.kwanza.dbtool.core.UpdateException;
import ru.kwanza.jeda.clusterservice.IClusterService;
import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.jeda.clusterservice.impl.db.orm.ClusterNode;
import ru.kwanza.jeda.clusterservice.impl.db.orm.ClusteredComponent;
import ru.kwanza.txn.api.spi.ITransactionManager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Alexander Guzanov
 */
public class DBClusterService implements IClusterService, ApplicationListener<ContextRefreshedEvent> {
    public static final String SUPERVISOR_NAME = "DBClusterService-Supervisor";
    public static final String WORKER_NAME = "DBClusterService-Worker";

    @Resource(name = "txn.ITransactionManager")
    private ITransactionManager tm;
    @Resource
    private DBClusterServiceDao dao;
    @Resource
    private ComponentRepository repository;

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


    @PostConstruct
    public void init() {
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

    private void initCurrentNode() {
        currentNode = dao.findOrCreateNode(new ClusterNode(currentNodeId, System.currentTimeMillis()));
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
                return new Thread(r, WORKER_NAME + "-" + counter.incrementAndGet());
            }
        });
    }

    public List<? extends ClusterNode> getActiveNodes() {
        return dao.selectActiveNodes();
    }

    public List<? extends Node> getPassiveNodes() {
        return dao.selectPassiveNodes();
    }

    public List<? extends Node> getNodes() {
        return dao.selectNodes();
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    public Map<String, IClusteredComponent> getRepository() {
        return repository.getComponents();
    }

    public Map<String, IClusteredComponent> getStartedComponents() {
        return repository.getStartedComponents();
    }

    public Map<String, IClusteredComponent> getStoppedComponents() {
        return repository.getStoppedComponents();
    }

    public <R> R criticalSection(IClusteredComponent component, Callable<R> callable) throws InterruptedException, InvocationTargetException {
        return null;
    }

    public <R> R criticalSection(IClusteredComponent component, Callable<R> callable, long waitTimeout, TimeUnit unit)
            throws InterruptedException, InvocationTargetException, TimeoutException {
        return null;
    }

    public void markReparied(IClusteredComponent component, Node node) {

    }

    public void registerComponent(IClusteredComponent component) {
        if (started) {
            throw new IllegalStateException("Can't regiter component " + component.getName() +
                    "! Supervisor " + SUPERVISOR_NAME + "is already started!");
        }

        dao.findOrCreateComponent(currentNode, component);
        repository.registerComponent(component);
    }

    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.info("Starting {} ...", SUPERVISOR_NAME);
        supervisor.start();
        started = true;
    }

    public class Supervisor extends Thread {
        public Supervisor() {
            super(SUPERVISOR_NAME);
            setDaemon(true);
        }

        @Override
        public void run() {
            logger.info("Started {}", SUPERVISOR_NAME);

            while (true) {
                try {
                    calcLastActivity();
                    processInitialState();
                    break;
                } catch (Throwable ex) {
                    continue;
                }
            }

            while (started && !isInterrupted()) {
                calcLastActivity();
                try {
                    try {
                        tm.begin();
                        leaseActivity();
                        checkPassiveComponents();
                        handleAlienComponents();
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

        private void handleAlienComponents() {
            leaseAlienComponents();
            findAlienStaleComponents();
        }

        private void leaseAlienComponents() {
            if (!repository.getAlienComponents().isEmpty()) {
                Collection<ClusteredComponent> items = dao.loadComponentsByKey(repository.getAlienComponents().keySet());
                List<ClusteredComponent> stopRepair = new ArrayList<ClusteredComponent>();
                for (ClusteredComponent item : items) {
                    if (item.getWaitForReturn()) {
                        item.clearMarkers();
                        item.setRepaired(true);
                        stopRepair.add(item);
                    } else {
                        repository.removeAlienComponent(item.getId());
                    }
                }


                try {
                    dao.updateComponents(stopRepair);
                } catch (UpdateException e) {
                    stopRepair = e.getUpdated();
                }

                for (ClusteredComponent component : stopRepair) {
                    //todo aguzanov move to worker
                    repository.getComponent(component.getName()).handleStopRepair(component.getNode());
                }

                try {
                    dao.updateComponents(repository.getAlienComponents().values());
                } catch (UpdateException e) {
                }

            }
        }

        private void findAlienStaleComponents() {
            List<ClusteredComponent> items = dao.selectAlienStaleComponents(currentNode);

            for (ClusteredComponent component : items) {
                component.clearMarkers();
                component.setHoldNodeId(currentNodeId);
            }

            try {
                dao.updateComponents(items);
            } catch (UpdateException e) {
                items = e.getUpdated();
            }

            for (ClusteredComponent component : items) {
                repository.addAlientComponent(component);
                IClusteredComponent cc = repository.getComponent(component.getName());
                //todo aguzanov перенести в workers
                cc.handleStartRepair(component.getNode());
            }
        }


        private void calcLastActivity() {
            lastActivityTs = System.currentTimeMillis() + failoverInterval;
        }

        private void processInitialState() {
            List<ClusteredComponent> items = dao.selectAllComponents(currentNode);
            filterComponentByState(items);
            leaseActivity();
            startActiveComponents();
        }

        private void startActiveComponents() {
            for (IClusteredComponent item : getStartedComponents().values()) {
                //todo aguzanov вынести в пул потоков
                item.handleStart();
            }
        }

        private void filterComponentByState(List<ClusteredComponent> items) {
            final long ts = System.currentTimeMillis();
            for (ClusteredComponent item : items) {
                if (item.getHoldNodeId() != null) {
                    if (item.getLastActivity() <= ts) {
                        item.clearMarkers();
                        repository.addActiveComponent(item);
                    } else {
                        repository.addPassiveComponent(item);
                    }
                } else {
                    repository.addActiveComponent(item);
                }
            }
        }

        private void leaseActivity() {
            try {
                updateActivity(repository.getActiveComponents());
            } catch (UpdateException e) {
                for (ClusteredComponent o : e.<ClusteredComponent>getConstrainted()) {
                    repository.addPassiveComponent(o);
                    if (repository.removeActiveComponent(o.getName())) {
                        repository.getComponent(o.getName()).handleStop();
                    }
                }
                for (ClusteredComponent o : e.<ClusteredComponent>getOptimistic()) {
                    repository.addPassiveComponent(o);
                    if (repository.removeActiveComponent(o.getName())) {
                        repository.getComponent(o.getName()).handleStop();
                    }
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
                repository.addActiveComponent(item);
                repository.removePassiveComponent(item.getName());
                //todo aguzanov do in worker
                repository.getComponent(item.getName()).handleStart();
            }
        }

        private void updateActivity(Collection<ClusteredComponent> items) throws UpdateException {
            for (ClusteredComponent component : items) {
                component.setLastActivity(lastActivityTs);
                component.clearMarkers();
            }

            dao.updateComponents(items);
        }

        private void checkPassiveComponents() {
            if (!repository.getPassiveComponents().isEmpty()) {
                Collection<ClusteredComponent> items = dao.selectWaitForReturn();
                List<ClusteredComponent> activateCandidates = findCandidateForActivation(items);
                activateCandidates(activateCandidates);
                dao.markWaitForReturn();
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
    }
}
