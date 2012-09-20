package ru.kwanza.jeda.core.threadmanager.shared;

import ru.kwanza.jeda.api.internal.IStageInternal;
import ru.kwanza.jeda.api.internal.ISystemManager;
import ru.kwanza.jeda.core.threadmanager.AbstractProcessingThread;
import ru.kwanza.jeda.core.threadmanager.AbstractThreadManager;
import ru.kwanza.jeda.core.threadmanager.shared.comparator.RoundRobinComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Guzanov Alexander
 */
public class SharedThreadManager extends AbstractThreadManager {
    public static final int DEFAULT_MAX_THREAD_COUNT = 50;

    private static final Logger logger = LoggerFactory.getLogger(SharedThreadManager.class);
    private long nameCounter = 0;
    private int wantedThreadCount = 0;
    private List<StageEntry> orderedStage = new ArrayList<StageEntry>();
    private Comparator<StageEntry> stageComparator = new RoundRobinComparator();

    public SharedThreadManager(String threadNamePrefix, ISystemManager manager) {
        super(threadNamePrefix, manager);
        setMaxThreadCount(DEFAULT_MAX_THREAD_COUNT);
    }


    public void adjustThreadCount(IStageInternal stage, int threadCount) {
        try {
            getLock().lockInterruptibly();
        } catch (InterruptedException e) {
            logger.warn("Thread interrupted", e);
            return;
        }

        try {
            StageEntry entry = findStageEntry(stage);
            if (entry == null) {
                entry = new StageEntry(stage);
                entry.setThreadCount(threadCount);
                orderedStage.add(entry);
                wantedThreadCount += entry.getThreadCount();
            } else {
                int delta = threadCount - entry.getThreadCount();
                entry.setThreadCount(threadCount);
                wantedThreadCount += delta;
            }

            resort();

            int delta = wantedThreadCount - pool.size();

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

    public Comparator<StageEntry> getStageComparator() {
        return stageComparator;
    }

    public void setStageComparator(Comparator<StageEntry> stageComparator) {
        this.stageComparator = stageComparator;
    }

    protected AbstractProcessingThread createProcessingThread(IStageInternal stage) {
        nameCounter++;
        return new SharedProcessingThread(getThreadNamePrefix() + "-" + nameCounter, getManager(), this);
    }

    StageEntry findStageEntry() {
        getLock().lock();
        try {
            resort();

            for (StageEntry result : orderedStage) {
                if (result.getThreadCount() <= 0) {
                    continue;
                }
                if (result.getCurrentThreadCount() >= result.getThreadCount() ||
                        !result.getStage().getQueue().isReady()) {
                    continue;
                }
                result.increaseCurrentThreadCount();
                result.setTs(System.currentTimeMillis());
                return result;
            }

            return null;
        } finally {
            getLock().unlock();
        }
    }

    private StageEntry findStageEntry(IStageInternal stage) {
        if (stage instanceof StageEntry.StageWrapper) {
            return ((StageEntry.StageWrapper) stage).entry();
        }
        for (StageEntry stageEntry : orderedStage) {
            if (stageEntry.getStage() == stage) {
                return stageEntry;
            }
        }

        return null;
    }

    private void resort() {
        if (stageComparator == null) return;

        try {
            Collections.sort(orderedStage, stageComparator);
        } catch (Throwable e) {
            logger.error("Error resorting stage in SharedThreadManager", e);
        }
    }
}
