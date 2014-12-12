package ru.kwanza.jeda.clusterservice.impl.db;

import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.Node;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static ru.kwanza.jeda.clusterservice.impl.db.DBClusterService.logger;
import static ru.kwanza.jeda.clusterservice.impl.db.WorkerController.Status.*;

/**
 * @author Alexander Guzanov
 */
public class WorkerController {
    private int attemptInterval;
    private int threadCount;
    private int keepAlive;

    private ExecutorService workerExecutor;
    private AtomicLong counter = new AtomicLong(0);

    private final ConcurrentMap<String, AbstractTask> tasks = new ConcurrentHashMap<String, AbstractTask>();


    public WorkerController(int threadCount, int attemptInterval, int keepAlive) {
        this.threadCount = threadCount;
        this.attemptInterval = attemptInterval;
        this.keepAlive = keepAlive;
    }

    public int getAttemptInterval() {
        return attemptInterval;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public void startComponent(String id, IClusteredComponent component) {
        createComponentTask(id, component).scheduleStart();
    }

    public void stopComponent(String id, IClusteredComponent component) {
        createComponentTask(id, component).scheduleStop();
    }

    public void startRepair(String id, IClusteredComponent component, Node node) {
        createComponentRepairTask(id, component, node).scheduleStart();
    }

    public void stopRepair(String id, IClusteredComponent component, Node node) {
        createComponentRepairTask(id, component, node).scheduleStop();
    }

    private AbstractTask createComponentRepairTask(String id, IClusteredComponent component, Node node) {
        AbstractTask task = tasks.get(id);
        if (task == null) {
            task = new ChangeComponentRepairStatusTask(id, node, component);
            if (tasks.putIfAbsent(id, task) != null) {
                task = tasks.get(id);
            }
        }
        return task;
    }

    private AbstractTask createComponentTask(String id, IClusteredComponent component) {
        AbstractTask task = tasks.get(id);
        if (task == null) {
            task = new ChangeComponentStatusTask(id, component);
            if (tasks.putIfAbsent(id, task) != null) {
                task = tasks.get(id);
            }
        }
        return task;
    }

    @PostConstruct
    public void init() {
        workerExecutor = new ThreadPoolExecutor(threadCount, threadCount, keepAlive,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new Thread(r, DBClusterService.WORKER_NAME + "-" + counter.incrementAndGet());
            }
        });
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        logger.info("Stopping worker threads ...");
        workerExecutor.shutdownNow();
        workerExecutor.awaitTermination(60000, TimeUnit.MILLISECONDS);
    }


    public enum Status {
        SCHEDULE_START,
        SCHEDULE_STOP,
        STARTING,
        STOPPING,
        COMPLETE
    }

    public abstract class AbstractTask implements Runnable {
        protected final IClusteredComponent component;
        private final String id;
        private AtomicReference<Status> status = new AtomicReference<Status>();


        public AbstractTask(String id, IClusteredComponent component) {
            this.id = id;
            this.component = component;
        }

        public void run() {
            while (isComplete()) {
                try {
                    work();
                } catch (Throwable ex) {
                    logger.error("Error executing task for component " + component.getName(), ex);
                    try {
                        Thread.sleep(attemptInterval);
                    } catch (InterruptedException e) {
                        break;
                    }
                    continue;
                }
            }
        }

        private boolean isComplete() {
            return status.get() != COMPLETE;
        }

        protected void work() {
            if (changeStatus(SCHEDULE_STOP, STOPPING)) {
                stopping();
            } else if (changeStatus(SCHEDULE_START, STARTING)) {
                starting();
            } else {
                logger.debug("Skip working for {}, current status", this.component.getName(), status.get());
            }
        }

        private void starting() {
            try {
                handleStart();
                completeIf(STARTING);
            } catch (Throwable ex) {
                if (changeStatus(STARTING, SCHEDULE_START)) {
                    throw ex;
                }
                completeIf(SCHEDULE_STOP);
            }
        }

        private void stopping() {
            try {
                handleStop();
                completeIf(STOPPING);
            } catch (Throwable ex) {
                if (changeStatus(STOPPING, SCHEDULE_STOP)) {
                    throw ex;
                }
                completeIf(SCHEDULE_START);
            }
        }

        protected abstract void handleStop();

        protected abstract void handleStart();

        void scheduleStart() {
            completeIf(SCHEDULE_STOP);

            if (changeStatus(null, SCHEDULE_START)) {
                schedule();
            } else if (changeStatus(STOPPING, SCHEDULE_START)) {
                logger.debug("Component {} is stopping now . Schedule it for start",id);
            } else if (changeStatus(COMPLETE, SCHEDULE_START)) {
                tasks.put(id, this);
                schedule();
                logger.debug("Component  {} was completed . Schedule it for start",id);
            }
        }

        void scheduleStop() {
            completeIf(SCHEDULE_START);

            if (changeStatus(null, SCHEDULE_STOP)) {
                schedule();
            } else if (changeStatus(STARTING, SCHEDULE_STOP)) {
                logger.debug("Component {} is starting now. Schedule it for start",id);
            } else if (changeStatus(COMPLETE, SCHEDULE_STOP)) {
                tasks.put(id, this);
                schedule();
                logger.debug("Component {} was completed . Schedule it for stop",id);
            }
        }

        private void schedule() {
            workerExecutor.execute(this);
        }

        private void completeIf(Status status) {
            if (this.status.compareAndSet(status, COMPLETE)) {
                tasks.remove(id);
            }
        }

        private boolean changeStatus(Status from, Status to) {
            return status.compareAndSet(from, to);
        }
    }

    private class ChangeComponentStatusTask extends AbstractTask {
        public ChangeComponentStatusTask(String id, IClusteredComponent component) {
            super(id, component);
        }

        protected void handleStop() {
            logger.info("Stopping component {}", component.getName());
            component.handleStop();
            logger.info("Stopped component {}", component.getName());
        }

        protected void handleStart() {
            logger.info("Starting component {}", component.getName());
            component.handleStart();
            logger.info("Started component {}", component.getName());
        }

    }

    private class ChangeComponentRepairStatusTask extends ChangeComponentStatusTask {
        private Node node;

        public ChangeComponentRepairStatusTask(String id, Node node, IClusteredComponent component) {
            super(id, component);
            this.node = node;
        }

        protected void handleStop() {
            logger.info("Stopping repair component {} for node {} ...", component.getName(), node.toString());
            component.handleStopRepair(node);
            logger.info("Stopped repair component {} for node {}", component.getName(), node.toString());
        }

        protected void handleStart() {
            logger.info("Starting repair component {} for node {} ...", component.getName(), node.toString());
            component.handleStartRepair(node);
            logger.info("Started repair component {} for node {}", component.getName(), node.toString());
        }
    }
}
