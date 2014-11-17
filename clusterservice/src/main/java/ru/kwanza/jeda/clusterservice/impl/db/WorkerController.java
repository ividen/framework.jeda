package ru.kwanza.jeda.clusterservice.impl.db;

import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.Node;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import static ru.kwanza.jeda.clusterservice.impl.db.DBClusterService.logger;

/**
 * @author Alexander Guzanov
 */
public class WorkerController {
    private int attemptCount;
    private int attemptInterval;
    private int threadCount;
    private int keepAlive;

    private ExecutorService workerExecutor;
    private AtomicLong counter = new AtomicLong(0);

    private final Map<String, ChangeComponentStatusTask> tasks = new HashMap<String, ChangeComponentStatusTask>();


    public WorkerController(int threadCount, int attemptCount, int attemptInterval, int keepAlive) {
        this.threadCount = threadCount;
        this.attemptCount = attemptCount;
        this.attemptInterval = attemptInterval;
        this.keepAlive = keepAlive;
    }

    public int getAttemptCount() {
        return attemptCount;
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
        synchronized (tasks) {
            ChangeComponentStatusTask task = tasks.get(id);
            if (task == null) {
                task = new ChangeComponentStatusTask(id, component);
                tasks.put(id, task);
            }
            task.scheduleStart();
        }
    }

    public void stopComponent(String id, IClusteredComponent component) {
        synchronized (tasks) {
            ChangeComponentStatusTask task = tasks.get(id);
            if (task == null) {
                task = new ChangeComponentStatusTask(id, component);
                tasks.put(id, task);
            }
            task.scheduleStop();
        }
    }

    public void startRepair(String id, IClusteredComponent component, Node node) {
        synchronized (tasks) {
            ChangeComponentStatusTask task = tasks.get(id);
            if (task == null) {
                task = new ChangeComponentRepairStatusTask(id, node, component);
                tasks.put(id, task);
            }
            task.scheduleStart();
        }
    }

    public void stopRepair(String id, IClusteredComponent component, Node node) {
        synchronized (tasks) {
            ChangeComponentStatusTask task = tasks.get(id);
            if (task == null) {
                task = new ChangeComponentRepairStatusTask(id, node, component);
                tasks.put(id, task);
            }
            task.scheduleStop();
        }
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


    public abstract class AbstractTask extends ReentrantLock implements Runnable {
        protected final IClusteredComponent component;
        private final String id;
        private volatile int start_or_stop;
        private volatile boolean complete = false;
        private volatile boolean scheduled = false;


        public AbstractTask(String id, IClusteredComponent component) {
            this.id = id;
            this.component = component;
        }

        public void run() {
            boolean success = false;
            int attemptCount = WorkerController.this.attemptCount;
            while (attemptCount > 0 && !success) {
                try {
                    work();
                    success = true;
                } catch (Throwable ex) {
                    logger.error("Error executing task for component " + component.getName(), ex);

                    attemptCount--;
                    try {
                        Thread.sleep(attemptInterval);
                    } catch (InterruptedException e) {
                        break;
                    }
                    continue;
                }
            }

            if (!success) {
                workerExecutor.execute(this);
            } else {
                synchronized (tasks) {
                    tasks.remove(id);
                }
            }
        }

        protected void work() {
            lock();
            try {
                if (start_or_stop > 0) {
                    handleStart();
                } else if (start_or_stop < 0) {
                    handleStop();
                }
                complete = true;
            } finally {
                unlock();
            }
        }

        protected abstract void handleStop();

        protected abstract void handleStart();

        void scheduleStart() {
            lock();
            try {
                if (start_or_stop <= 0) {
                    start_or_stop = start_or_stop + 1;
                    trySchedule();
                }
            } finally {
                unlock();
            }
        }

        void scheduleStop() {
            lock();
            try {
                if (start_or_stop >= 0) {
                    start_or_stop = start_or_stop - 1;
                    trySchedule();
                }
            } finally {
                unlock();
            }
        }

        void trySchedule() {
            if (complete || !scheduled) {
                scheduled = true;
                workerExecutor.execute(this);
            }
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
