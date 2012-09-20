package ru.kwanza.jeda.core.resourcecontroller;

import ru.kwanza.jeda.api.internal.IStageInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Guzanov Alexander
 */
class WaitingForBatchFilling {
    private static final Logger logger = LoggerFactory.getLogger(WaitingForBatchFilling.class);
    public static WaitingForBatchFilling instance = new WaitingForBatchFilling();
    private ConcurrentHashMap<IStageInternal, Task> tasks = new ConcurrentHashMap<IStageInternal, Task>();
    private Timer timer = new Timer("WaitingForBatchFilling");

    public static WaitingForBatchFilling getInstance() {
        return instance;
    }

    private class Task extends TimerTask {
        private IStageInternal stage;
        private int maxThreadCount;

        private Task(IStageInternal stage, int maxThreadCount) {
            this.stage = stage;
            this.maxThreadCount = maxThreadCount;
        }

        public void doWork() {
            if (stage.getQueue().isReady()) {
                int threadCount = stage.getQueue().getEstimatedCount() / stage.getResourceController().getBatchSize();
                if (threadCount == 0) {
                    threadCount = 1;
                }
                if (threadCount > maxThreadCount) {
                    threadCount = maxThreadCount;
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("Adjust thread count for Stage({}) by timer: threadCount={}", stage.getName(), threadCount);
                }
                stage.getThreadManager().adjustThreadCount(stage, threadCount);
            }
        }

        @Override
        public void run() {
            try {
                doWork();
            } catch (Throwable e) {
                logger.error("Error processing timeout for batch filling", e);
            }
        }
    }


    public void schedule(IStageInternal stage, int maxThreadCount, long timeOut) {
        if (tasks.contains(stage)) {
            return;
        }

        Task value = new Task(stage, maxThreadCount);
        if (null == tasks.putIfAbsent(stage, value)) {
            timer.scheduleAtFixedRate(value, timeOut, timeOut);
        }
    }
}
