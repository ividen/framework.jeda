package ru.kwanza.jeda.clusterservice.impl.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import ru.kwanza.dbtool.core.UpdateException;
import ru.kwanza.jeda.clusterservice.IClusterService;
import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.jeda.clusterservice.impl.db.orm.ComponentEntity;
import ru.kwanza.jeda.clusterservice.impl.db.orm.NodeEntity;
import ru.kwanza.txn.api.spi.ITransactionManager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

    private NodeEntity currentNode;
    private Integer currentNodeId;
    private volatile long lastActivityTs;

    private static Logger logger = LoggerFactory.getLogger(DBClusterService.class);

    private long failoverInterval;
    private long activityInterval;
    private int repairThreadCount;
    private int workerAttemptCount;
    private int workerAttemptInterval;

    private ExecutorService workerExecutor;
    private AtomicLong counter = new AtomicLong(0);
    private volatile boolean started = false;
    private Supervisor supervisor;

    private Lock repairLock = new ReentrantLock();


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
        logger.info("Stopping worker threads ...");
        workerExecutor.shutdownNow();
        workerExecutor.awaitTermination(60000, TimeUnit.MILLISECONDS);

    }

    private void initCurrentNode() {
        currentNode = dao.findOrCreateNode(new NodeEntity(currentNodeId, System.currentTimeMillis()));
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

    public int getWorkerAttemptCount() {
        return workerAttemptCount;
    }

    public void setWorkerAttemptCount(int workerAttemptCount) {
        this.workerAttemptCount = workerAttemptCount;
    }

    public int getWorkerAttemptInterval() {
        return workerAttemptInterval;
    }

    public void setWorkerAttemptInterval(int workerAttemptInterval) {
        this.workerAttemptInterval = workerAttemptInterval;
    }

    private void initSupervisors() {
        supervisor = new Supervisor();

        workerExecutor = new ThreadPoolExecutor(repairThreadCount, repairThreadCount, activityInterval, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new Thread(r, WORKER_NAME + "-" + counter.incrementAndGet());
            }
        });
    }

    public List<? extends NodeEntity> getActiveNodes() {
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

    public Map<String, IClusteredComponent> getActiveComponents() {
        return repository.getActiveComponents();
    }

    public Map<String, IClusteredComponent> getPassiveComponents() {
        return repository.getPassiveComponents();
    }

    public <R> R criticalSection(IClusteredComponent component, Callable<R> callable)
            throws InvocationTargetException, ComponentInActiveExcetion {
        checkActivity(component);
        R result;
        try {
            result = callable.call();
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }

        checkActivity(component);
        return result;
    }

    private void checkActivity(IClusteredComponent component) throws ComponentInActiveExcetion {
        if (!repository.isActive(component.getName())) {
            throw new ComponentInActiveExcetion("Component " + component.getName() + " is inactive!");
        }
    }

    public boolean markRepaired(IClusteredComponent component, Node node) {
        repairLock.lock();
        try {
            ComponentEntity componentEntity = repository.getAlienEntities().get(ComponentEntity.createId(node.getId(), component.getName()));
            if (componentEntity != null) {
                componentEntity.clearMarkers();
                componentEntity.setRepaired(true);
                componentEntity.setLastActivity(0l);
                try {
                    dao.updateComponents(Collections.singleton(componentEntity));
                    repository.removeAlienComponent(componentEntity.getId());
                    component.handleStopRepair(node);
                } catch (UpdateException e) {
                    //todo aguzanov log error
                    return false;
                }
            }
        } finally {
            repairLock.unlock();
        }
        return true;
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
            repairLock.lock();
            try {
                leaseAlienComponents();
                findAlienStaleComponents();
            } finally {
                repairLock.unlock();
            }
        }

        private void leaseAlienComponents() {
            if (!repository.getAlienEntities().isEmpty()) {
                Collection<ComponentEntity> items = dao.loadComponentsByKey(repository.getAlienEntities().keySet());
                List<ComponentEntity> stopRepair = new ArrayList<ComponentEntity>();
                for (ComponentEntity item : items) {
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

                for (ComponentEntity component : stopRepair) {
                    //todo aguzanov move to worker
                    repository.getComponent(component.getName()).handleStopRepair(component.getNode());
                }

                try {
                    dao.updateComponents(repository.getAlienEntities().values());
                } catch (UpdateException e) {
                }

            }
        }

        private void findAlienStaleComponents() {
            List<ComponentEntity> items = dao.selectAlienStaleComponents(currentNode);

            for (ComponentEntity component : items) {
                component.clearMarkers();
                component.setHoldNodeId(currentNodeId);
            }

            try {
                dao.updateComponents(items);
            } catch (UpdateException e) {
                items = e.getUpdated();
            }

            for (ComponentEntity component : items) {
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
            List<ComponentEntity> items = dao.selectAllComponents(currentNode);
            filterComponentByState(items);
            leaseActivity();
            startActiveComponents();
        }

        private void startActiveComponents() {
            for (IClusteredComponent item : getActiveComponents().values()) {
                startComponent(item);
            }
        }

        private void filterComponentByState(List<ComponentEntity> items) {
            final long ts = System.currentTimeMillis();
            for (ComponentEntity item : items) {
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
                updateActivity(repository.getActiveEntities());
            } catch (UpdateException e) {
                for (ComponentEntity o : e.<ComponentEntity>getConstrainted()) {
                    repository.addPassiveComponent(o);
                    if (repository.removeActiveComponent(o.getName())) {
                        stopComponent(repository.getComponent(o.getName()));
                    }
                }
                for (ComponentEntity o : e.<ComponentEntity>getOptimistic()) {
                    repository.addPassiveComponent(o);
                    if (repository.removeActiveComponent(o.getName())) {
                        stopComponent(repository.getComponent(o.getName()));
                    }
                }
            }
        }

        private void activateCandidates(List<ComponentEntity> activateCandidates) {
            try {
                updateActivity(activateCandidates);
            } catch (UpdateException e) {
                activateCandidates = e.getUpdated();
            }

            for (ComponentEntity item : activateCandidates) {
                repository.addActiveComponent(item);
                repository.removePassiveComponent(item.getName());
                startComponent(repository.getComponent(item.getName()));
            }
        }

        private void startComponent(IClusteredComponent component) {
            workerExecutor.execute(new StartWorker(component));
        }

        private void stopComponent(IClusteredComponent component) {
            workerExecutor.execute(new StopWorker(component));
        }

        private void startRepair(IClusteredComponent component, Node node) {
            workerExecutor.execute(new StartRepairWorker(component, node));
        }

        private void stopRepair(IClusteredComponent component, Node node) {
            workerExecutor.execute(new StopRepairWorker(component, node));
        }

        private void updateActivity(Collection<ComponentEntity> items) throws UpdateException {
            for (ComponentEntity component : items) {
                component.setLastActivity(lastActivityTs);
                component.clearMarkers();
            }

            dao.updateComponents(items);
        }

        private void checkPassiveComponents() {
            if (!repository.getPassiveEntities().isEmpty()) {
                Collection<ComponentEntity> items = dao.selectWaitForReturn();
                List<ComponentEntity> activateCandidates = findCandidateForActivation(items);
                activateCandidates(activateCandidates);
                dao.markWaitForReturn();
            }
        }

        private List<ComponentEntity> findCandidateForActivation(Collection<ComponentEntity> items) {
            List<ComponentEntity> activateCandidates = new ArrayList<ComponentEntity>();
            for (ComponentEntity item : items) {
                if (item.getHoldNodeId() == null) {
                    activateCandidates.add(item);
                }
            }
            return activateCandidates;
        }
    }

    public abstract class AbstractWorker implements Runnable {
        private IClusteredComponent component;

        public AbstractWorker(IClusteredComponent component) {
            this.component = component;
        }

        public void run() {
            boolean success = false;
            int attemptCount = getWorkerAttemptCount();
            while (attemptCount > 0 && !success) {
                try {
                    synchronized (repository.getMonitor(component)) {
                        work(component);
                    }
                    success = true;
                } catch (Throwable ex) {
                    attemptCount--;
                    try {
                        Thread.sleep(getWorkerAttemptInterval());
                    } catch (InterruptedException e) {
                        break;
                    }
                    continue;
                }
            }

            if (!success) workerExecutor.execute(this);
        }

        protected abstract void work(IClusteredComponent component);
    }

    public class StartWorker extends AbstractWorker {
        public StartWorker(IClusteredComponent component) {
            super(component);
        }

        @Override
        protected void work(IClusteredComponent component) {
            if (repository.isActive(component.getName())) {
                component.handleStart();
            }
        }
    }

    public class StopWorker extends AbstractWorker {

        public StopWorker(IClusteredComponent component) {
            super(component);
        }

        @Override
        protected void work(IClusteredComponent component) {
            if (!repository.isActive(component.getName())) {
                component.handleStop();
            }
        }
    }

    public class StartRepairWorker extends AbstractWorker {
        private Node node;

        public StartRepairWorker(IClusteredComponent component, Node node) {
            super(component);
            this.node = node;
        }


        @Override
        protected void work(IClusteredComponent component) {
            component.handleStartRepair(node);
        }
    }

    public class StopRepairWorker extends AbstractWorker {
        private Node node;

        public StopRepairWorker(IClusteredComponent component, Node node) {
            super(component);
            this.node = node;
        }

        @Override
        protected void work(IClusteredComponent component) {
            component.handleStopRepair(node);
        }
    }


}
