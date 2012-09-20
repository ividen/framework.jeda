package ru.kwanza.jeda.core.threadmanager.stage;

import ru.kwanza.jeda.api.internal.IStageInternal;
import ru.kwanza.jeda.api.internal.ISystemManager;
import ru.kwanza.jeda.core.threadmanager.AbstractProcessingThread;
import ru.kwanza.jeda.core.threadmanager.AbstractThreadManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Guzanov Alexander
 */
public class StageThreadManager extends AbstractThreadManager {
    private static final Logger logger = LoggerFactory.getLogger(StageThreadManager.class);
    private long nameCounter = 0;

    public StageThreadManager(String threadNamePrefix, ISystemManager manager) {
        super(threadNamePrefix, manager);
    }

    public void adjustThreadCount(IStageInternal stage, int threadCount) {
        try {
            getLock().lockInterruptibly();
        } catch (InterruptedException e) {
            logger.warn("Thread interrupted", e);
            return;
        }

        try {
            int delta = threadCount - pool.size();

            if (delta > 0) {
                if (pool.size() + delta > getMaxThreadCount()) {
                    delta = getMaxThreadCount() - pool.size();
                }
                increaseTheadPool(stage, delta);
            } else if (delta < 0) {
                decreaseThreadPool(stage, delta);
            }

            wakeUpThreads();
        } finally {
            getLock().unlock();
        }
    }

    protected AbstractProcessingThread createProcessingThread(IStageInternal stage) {
        nameCounter++;
        return new StageProcessingThread(getThreadNamePrefix() + "-" + nameCounter, getManager(), this, stage);
    }
}
