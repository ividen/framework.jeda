package ru.kwanza.jeda.clusterservice.impl.db;

import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.Node;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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

    private ConcurrentMap<String, ChangeComponentStatusTask> tasks = new ConcurrentHashMap<String, ChangeComponentStatusTask>();


    public int getAttemptCount() {
        return attemptCount;
    }

    public int getAttemptInterval() {
        return attemptInterval;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public void setAttemptInterval(int attemptInterval) {
        this.attemptInterval = attemptInterval;
    }


    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }

    public void startComponent(String id, IClusteredComponent component) {
        ChangeComponentStatusTask task = new ChangeComponentStatusTask(component);
        if (tasks.putIfAbsent(id, task) != null) {
            task = tasks.get(id);
        }

        task.scheduleStart();
    }

    public void stopComponent(String id, IClusteredComponent component) {
        ChangeComponentStatusTask task = new ChangeComponentStatusTask(component);
        if (tasks.putIfAbsent(id, task) != null) {
            task = tasks.get(id);
        }

        task.scheduleStop();
    }

    public void startRepair(String id, IClusteredComponent component, Node node) {
        ChangeComponentStatusTask task = new ChangeComponentRepairStatusTask(node, component);
        if (tasks.putIfAbsent(id, task) != null) {
            task = tasks.get(id);
        }

        task.scheduleStart();
    }

    public void stopRepair(String id, IClusteredComponent component, Node node) {
        ChangeComponentStatusTask task = new ChangeComponentRepairStatusTask(node, component);
        if (tasks.putIfAbsent(id, task) != null) {
            task = tasks.get(id);
        }

        task.scheduleStop();
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
        private IClusteredComponent component;

        public AbstractTask(IClusteredComponent component) {
            this.component = component;
        }

        public void run() {
            boolean success = false;
            int attemptCount = WorkerController.this.attemptCount;
            while (attemptCount > 0 && !success) {
                try {
                    work(component);
                    success = true;
                } catch (Throwable ex) {
                    attemptCount--;
                    try {
                        Thread.sleep(attemptInterval);
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

    private class ChangeComponentStatusTask extends AbstractTask {
        private volatile int start_or_stop;
        private volatile boolean complete = false;
        private volatile boolean scheduled = false;

        public ChangeComponentStatusTask(IClusteredComponent component) {
            super(component);
        }

        @Override
        protected void work(IClusteredComponent component) {
            lock();
            try {
                if (start_or_stop > 0) {
                    handleStart(component);
                } else if (start_or_stop < 0) {
                    handleStop(component);
                }
                complete = true;
            } finally {
                unlock();
            }
        }

        protected void handleStop(IClusteredComponent component) {
            component.handleStop();
        }

        protected void handleStart(IClusteredComponent component) {
            component.handleStart();
        }

        void scheduleStart() {
            lock();
            try {
                start_or_stop = start_or_stop + 1;
                trySchedule();
            } finally {
                unlock();
            }
        }

        void scheduleStop() {
            lock();
            try {
                start_or_stop = start_or_stop - 1;
                trySchedule();
            } finally {
                unlock();
            }
        }

        void trySchedule() {
            if (!complete || !scheduled) {
                scheduled = true;
                workerExecutor.execute(this);
            }
        }
    }

    private class ChangeComponentRepairStatusTask extends ChangeComponentStatusTask {
        private Node node;

        public ChangeComponentRepairStatusTask(Node node, IClusteredComponent component) {
            super(component);
            this.node = node;
        }

        @Override
        protected void handleStop(IClusteredComponent component) {
            component.handleStopRepair(node);
        }

        @Override
        protected void handleStart(IClusteredComponent component) {
            component.handleStartRepair(node);
        }
    }
}
