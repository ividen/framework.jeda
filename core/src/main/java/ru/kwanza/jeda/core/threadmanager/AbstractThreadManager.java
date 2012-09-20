package ru.kwanza.jeda.core.threadmanager;

import ru.kwanza.jeda.api.internal.IStageInternal;
import ru.kwanza.jeda.api.internal.ISystemManager;
import ru.kwanza.jeda.api.internal.IThreadManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Guzanov Alexander
 */
public abstract class AbstractThreadManager implements IThreadManager {
    private static final int DEFAULT_MAX_SINGLE_EVENT_ATTEMPT = 3;


    private static final Logger logger = LoggerFactory.getLogger(AbstractThreadManager.class);

    protected List<AbstractProcessingThread> pool = new ArrayList<AbstractProcessingThread>();
    protected List<AbstractProcessingThread> inActive = new ArrayList<AbstractProcessingThread>();
    private String threadNamePrefix;
    private ISystemManager manager;
    private long idleTimeout = 1 * 60 * 1000l;
    private ReentrantLock lock = new ReentrantLock();
    private Condition wakeUpCondition = lock.newCondition();
    private int maxThreadCount = Runtime.getRuntime().availableProcessors();
    private int maxSingleEventAttempt = DEFAULT_MAX_SINGLE_EVENT_ATTEMPT;

    protected AbstractThreadManager(String threadNamePrefix, ISystemManager manager) {
        this.threadNamePrefix = threadNamePrefix;
        this.manager = manager;
    }

    protected abstract AbstractProcessingThread createProcessingThread(IStageInternal stage);

    public abstract void adjustThreadCount(IStageInternal stage, int threadCount);

    public int getThreadCount() {
        return pool.size();
    }

    public final ReentrantLock getLock() {
        return lock;
    }

    public final Condition getWakeUpCondition() {
        return wakeUpCondition;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public ISystemManager getManager() {
        return manager;
    }


    public int getMaxSingleEventAttempt() {
        return maxSingleEventAttempt;
    }

    public void setMaxSingleEventAttempt(int maxSingleEventAttempt) {
        this.maxSingleEventAttempt = maxSingleEventAttempt;
    }

    public int getMaxThreadCount() {
        return maxThreadCount;
    }

    public void setMaxThreadCount(int maxThreadCount) {
        this.maxThreadCount = maxThreadCount;
    }

    public String getThreadNamePrefix() {
        return threadNamePrefix;
    }

    protected final void wakeUpThreads() {
        getLock().lock();
        try {
            getWakeUpCondition().signalAll();
        } finally {
            getLock().unlock();
        }
    }

    protected void decreaseThreadPool(IStageInternal stage, int delta) {
        getLock().lock();
        try {
            for (int i = 0; i < -delta; i++) {
                int index = pool.size() - 1;
                AbstractProcessingThread thread = pool.get(index);
                thread.setActive(false);
                inActive.add(thread);
                if (logger.isDebugEnabled()) {
                    logger.debug("Get thread release signal for Stage {}, Thread {} marked for deactivating",
                            stage.getName(), thread.getName());
                }
                pool.remove(index);
            }
        } finally {
            getLock().unlock();
        }
    }

    protected boolean finishThread(AbstractProcessingThread thread) {
        getLock().lock();
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Finishing inactive thread {}", thread.getName());
            }
            return inActive.remove(thread);
        } finally {
            getLock().unlock();
        }
    }

    protected void increaseTheadPool(IStageInternal stage, int delta) {
        getLock().lock();
        try {
            for (int i = 0; i < delta; i++) {
                AbstractProcessingThread thread;
                if (!inActive.isEmpty()) {
                    thread = inActive.remove(inActive.size() - 1);
                    thread.setActive(true);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Acquired thread for Stage {}. Reactivate thread {}",
                                stage.getName(), thread.getName());
                    }
                } else {
                    thread = createProcessingThread(stage);
                    thread.start();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Acquired new thread {} for Stage {}",
                                thread.getName(), stage.getName());
                    }
                }
                pool.add(thread);
            }
        } finally {
            getLock().unlock();
        }
    }
}
