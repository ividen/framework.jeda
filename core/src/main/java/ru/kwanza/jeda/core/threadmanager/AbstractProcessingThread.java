package ru.kwanza.jeda.core.threadmanager;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.MarkTransactionRollbackException;
import ru.kwanza.jeda.api.internal.IResourceController;
import ru.kwanza.jeda.api.internal.IStageInternal;
import ru.kwanza.jeda.api.internal.ISystemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Guzanov Alexander
 */
public abstract class AbstractProcessingThread<TM extends AbstractThreadManager> extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(AbstractProcessingThread.class);
    private volatile boolean active = true;
    private ReentrantLock mainLock = new ReentrantLock();
    private Condition notActive = mainLock.newCondition();
    private ISystemManager manager;
    private TM threadManager;

    protected AbstractProcessingThread(String name, ISystemManager manager, TM threadManager) {
        super(name);
        this.manager = manager;
        this.threadManager = threadManager;
    }

    public abstract IStageInternal getStage();

    public void run() {
        boolean finished = false;
        while (!finished && !isInterrupted()) {
            doWork();
            if (isInterrupted()) {
                break;
            }
            try {
                mainLock.lockInterruptibly();
            } catch (InterruptedException e) {
                break;
            }
            try {
                long ts = System.currentTimeMillis();
                long idleTimeout = threadManager.getIdleTimeout();
                long timeout = 0;
                while (timeout < idleTimeout && !active) {
                    notActive.await(idleTimeout - timeout, TimeUnit.MILLISECONDS);
                    timeout = System.currentTimeMillis() - ts;
                }
                if (!active) {
                    finished = threadManager.finishThread(this);
                }
            } catch (InterruptedException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Thread interuppted", e);
                }
                break;
            } finally {
                mainLock.unlock();
            }
        }
    }

    public TM getThreadManager() {
        return threadManager;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        try {
            mainLock.lockInterruptibly();
        } catch (InterruptedException e) {
            return;
        }
        this.active = active;
        try {
            if (active) {
                notActive.signal();
            }
        } finally {
            mainLock.unlock();
        }
    }

    private void beginTx(IStageInternal stage) {
        if (stage.hasTransaction()) {
            manager.getTransactionManager().begin();
        }
    }

    private boolean checkEvents(IStageInternal stage, Collection events, boolean findError) {
        if (events == null || events.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.warn("Take return empty event list for stage({})", stage.getName());
            }
            return false;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Begin processing events count {} for stage({})", events.size(), stage.getName());
        }

        return !(findError && events.size() == 1) || checkIsVeryDangerous(stage, events);
    }

    private boolean checkIsVeryDangerous(IStageInternal stage, Collection<IEvent> events) {
        IEvent singleEvent = events.iterator().next();
        ErrorController.DangerousEntry dangerousEntry = ErrorController
                .getInstance().findDangerousElement(stage, singleEvent);
        if (dangerousEntry != null && dangerousEntry.getAttempts() > threadManager.getMaxSingleEventAttempt()) {
            logger.error("Skip event {} it failed many times for stage {}",
                    singleEvent, stage.getName());

            return false;
        }
        return true;
    }

    private void commitTx(IStageInternal stage) {
        if (stage.hasTransaction()) {
            manager.getTransactionManager().commit();
        }
    }

    private void doWork() {
        while (active && !isInterrupted()) {
            IStageInternal stage = waitForStage();
            if (!process(stage)) {
                break;
            }
        }
    }

    protected boolean process(IStageInternal stage) {
        if (stage == null || !active || isInterrupted()) {
            return false;
        }
        manager.setCurrentStage(stage);
        try {
            IResourceController resourceController = stage.getResourceController();

            long ts = System.currentTimeMillis();
            boolean findError = false;
            int batchSize = resourceController.getBatchSize();
            if (batchSize == 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Zero batchSize for Stage({}), thread({})", new Object[]{stage.getName(),
                            this.getName()});
                }
                return true;
            }
            int estimateCount = 0;

            while (true) {
                if (!stage.getQueue().isReady()) {
                    break;
                }
                beginTx(stage);
                Collection<IEvent> events = getEvents(stage, batchSize);
                boolean execute = checkEvents(stage, events, findError);
                if (execute) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Begin processing events(count={}) for Stage({}), thread({})", new Object[]{stage.getName(),
                                events.size(), this.getName()});
                    }

                    try {
                        stage.getProcessor().process(events);
                    } catch (Throwable e) {
                        logger.error(MessageFormat.format("Error events(count={1}) for Stage({2}), thread({3})",
                                stage.getName(),
                                events.size(), this.getName()), e);

                        resourceController.throughput(events.size(), batchSize, System.currentTimeMillis() - ts, false);
                        rollbackAll(stage, e);
                        if (!stage.hasTransaction()) {
                            if (logger.isTraceEnabled()) {
                                logger.trace("Stage({}) doesn't support transactional execution. Skip failed events({})",
                                        stage.getName(), events);
                            } else {
                                logger.warn("Stage({}) doesn't support transactional execution. Skip failed events(count={})",
                                        stage.getName(), events.size());
                            }
                            break;
                        }
                        if ((e instanceof MarkTransactionRollbackException)) {
                            logger.warn("Transaction was marked for rollback.");
                            break;
                        }
                        if (!findError) {
                            estimateCount = events.size();
                            batchSize = events.size() / 2;
                            if (logger.isTraceEnabled()) {
                                logger.trace("FindingError:Was found error in events {} ,decrease batch={}", events, batchSize);
                            }
                        } else {
                            if (events.size() == 1) {
                                IEvent error = events.iterator().next();
                                if (logger.isTraceEnabled()) {
                                    logger.trace("FindingError:Was found error in event {} , register it as dangerous", error);
                                }
                                ErrorController.getInstance().registerDangerousElement(stage, error);
                            }

                            batchSize = batchSize / 2;
                            if (logger.isTraceEnabled()) {
                                logger.trace("FindingError:Still error in events {} ,decrease batch={}", events, batchSize);
                            }
                        }

                        if (batchSize == 0) {
                            batchSize = 1;
                        }
                        findError = true;
                        continue;
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("Finish processing events count {} for stage({})", events.size(), stage.getName());
                    }

                    if (findError) {
                        estimateCount -= events.size();
                        if (estimateCount <= 0) {
                            findError = false;
                            if (logger.isTraceEnabled()) {
                                logger.trace("FindingError: Estimating count of dangerous elements exceeded, finding errors finished",
                                        events, batchSize);
                            }
                        } else {
                            batchSize = estimateCount;
                            if (logger.isTraceEnabled()) {
                                logger.trace("FindingError: Batch was successful set batch ={}", events, batchSize);
                            }
                        }
                    }
                }

                commitTx(stage);
                calculateThroughput(resourceController, ts, batchSize, events);
                threadManager.adjustThreadCount(stage, resourceController.getThreadCount());
                if (execute && findError) {
                    continue;
                }
                break;
            }
        } finally {
            manager.setCurrentStage(null);
        }

        return true;
    }

    private void calculateThroughput(IResourceController resourceController, long ts, int batchSize, Collection<IEvent> events) {
        int size = events == null ? 0 : events.size();
        resourceController.throughput(size, batchSize, System.currentTimeMillis() - ts, true);
    }

    private Collection<IEvent> getEvents(IStageInternal stage, int batchSize) {
        try {
            return stage.getQueue().take(batchSize);
        } catch (Throwable e) {
            logger.error("Error taking events from Stage({})", stage.getName());
            logger.error("Error taking events", e);
            return null;
        }
    }

    private void rollbackAll(IStageInternal stage, Throwable e) {
        logger.error("Error processing events for Stage({})", stage.getName());
        logger.error("Error processing events", e);
        manager.getTransactionManager().rollbackAllActive();
    }

    private IStageInternal waitForStage() {
        IStageInternal stage = null;
        try {
            threadManager.getLock().lockInterruptibly();
        } catch (InterruptedException e) {
            logger.warn("Thread {} interrupted", getName());
            return null;
        }
        try {
            while (active && ((stage = getStage()) == null)) {
                try {
                    threadManager.getWakeUpCondition().await();
                } catch (InterruptedException e) {
                    logger.warn("Thread {} interrupted", getName());
                    break;
                }
            }
        } finally {
            threadManager.getLock().unlock();
        }

        return stage;
    }
}
