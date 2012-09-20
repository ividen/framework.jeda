package ru.kwanza.jeda.persistentqueue;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Guzanov Alexander
 */
public class QueueEventsTransfer {
    private static final QueueEventsTransfer instance = new QueueEventsTransfer();

    private LinkedBlockingQueue<Runnable> workQueue;
    private ThreadPoolExecutor executor;

    public static QueueEventsTransfer getInstance() {
        return instance;
    }

    public QueueEventsTransfer() {
        workQueue = new LinkedBlockingQueue<Runnable>();
        start();
    }

    public void schedule(PersistentQueue persistentQueue, long nodeId, long lastActivity) {
        executor.execute(new QueueEventsTransferRunnable(persistentQueue, nodeId, lastActivity));
    }

    public void setMaxThreadCount(int threadCount) {
        executor.setCorePoolSize(threadCount);
        executor.setMaximumPoolSize(threadCount);
    }

    public int getMaxThreadCount() {
        return executor.getCorePoolSize();
    }

    void shutdown() {
        executor.shutdownNow();
    }

    void start() {
        executor = new ThreadPoolExecutor(1, 1, 1000, TimeUnit.MILLISECONDS, workQueue);
    }

}
