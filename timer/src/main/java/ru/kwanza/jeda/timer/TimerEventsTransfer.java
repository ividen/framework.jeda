package ru.kwanza.jeda.timer;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Guzanov Alexander
 */
public class TimerEventsTransfer {
    private static final TimerEventsTransfer instance = new TimerEventsTransfer();

    private LinkedBlockingQueue<Runnable> workQueue;
    private ThreadPoolExecutor executor;

    public static TimerEventsTransfer getInstance() {
        return instance;
    }

    public TimerEventsTransfer() {
        workQueue = new LinkedBlockingQueue<Runnable>();
        start();
    }

    public void schedule(AbstractTimer persistentQueue, long nodeId, long lastActivity) {
        executor.execute(new TimerEventsTransferRunnable(persistentQueue, nodeId, lastActivity));
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
