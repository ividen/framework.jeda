package ru.kwanza.jeda.timerservice.pushtimer.consuming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kwanza.jeda.timerservice.pushtimer.LockHelper;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClass;
import ru.kwanza.jeda.timerservice.pushtimer.consuming.supervisor.ConsumerSupervisorThread;
import ru.kwanza.jeda.timerservice.pushtimer.memory.FiredTimersMemoryStorage;
import ru.kwanza.jeda.timerservice.pushtimer.TimerEntity;
import ru.kwanza.jeda.timerservice.pushtimer.dao.IFetchCursor;
import ru.kwanza.jeda.timerservice.pushtimer.monitoring.EventStatistic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Michael Yeskov
 */
public class ConsumerThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerThread.class);

    private NodeId nodeId;
    private TimerClass timerClass;
    private ConsumerSupervisorThread supervisor;
    private ConsumerConfig config;
    private FiredTimersMemoryStorage firedTimersMemoryStorage;
    private EventStatistic fetchStats;

    private volatile boolean alive = true;
    private volatile boolean idle = true;

    private ReentrantLock stateLock = new ReentrantLock(); //potentially "long lock" - using lockInterruptibly (closing connection under lock)
    private Condition sleepCondition = stateLock.newCondition();

    private ReentrantLock processingLock = new ReentrantLock(); //potentially "long lock" - using lockInterruptibly (working with db under lock)


    private long initialLeftBorder = 1; //inclusive
    private long leftBorder = 1; //inclusive
    private long rightBorder = 0; //inclusive

    private volatile ConsumerState consumerState;

    private IFetchCursor fetchCursor = null;



    public ConsumerThread(int id, NodeId nodeId, TimerClass timerClass, ConsumerSupervisorThread supervisor, ConsumerConfig config, FiredTimersMemoryStorage firedTimersMemoryStorage, EventStatistic fetchStats) {
        super("TimerConsumer-" + timerClass.getTimerClassName() + "-" + nodeId + "-" + id);
        this.nodeId = nodeId;
        this.timerClass = timerClass;
        this.supervisor = supervisor;
        this.config = config;
        this.firedTimersMemoryStorage = firedTimersMemoryStorage;
        this.fetchStats = fetchStats;
        consumerState = ConsumerState.IDLE;
    }

    private void doWork() {
        if (leftBorder > rightBorder) {
            changeSateToIdleIfNotSuspended();
            return;
        }

        if (fetchCursor == null || !fetchCursor.isOpen()) {
            fetchCursor = timerClass.getDbTimerDAO().createFetchCursor(leftBorder, rightBorder, nodeId.getEffectiveNodeId());
            fetchCursor.open();
        }


        long remainingCount = timerClass.getDbTimerDAO().getFetchSize();
        if (firedTimersMemoryStorage.canAccept(remainingCount)) {
            try {
                List<TimerEntity> firedTimers = new ArrayList<TimerEntity>();
                boolean sourceEnded  = fetchCursor.fetchInto(firedTimers);
                if (firedTimers.size() > 0) {
                    fetchStats.registerEvents(firedTimers.size());
                    firedTimersMemoryStorage.accept(firedTimers, nodeId.getEffectiveNodeId());
                    remainingCount -= firedTimers.size();
                }

                if (sourceEnded) {
                    leftBorder = rightBorder + 1; //end of source marker
                    changeSateToIdleIfNotSuspended(); //can be concurrently suspended
                } else {
                    leftBorder = fetchCursor.getCurrentLeftBorder();
                }
            } finally {
                firedTimersMemoryStorage.forgetCanAccept(remainingCount);
            }
        } else {
            if (!firedTimersMemoryStorage.isInSingleConsumerMode()) {
                fetchCursor.close(); //if we are in single consumer mode we leave  cursor open
            }
            pauseConsume(); //Supervisor will fire us(or not us) on next tick.
        }
    }

    private boolean changeSateToIdleIfNotSuspended() {
        LockHelper.lockInterruptibly(stateLock);
        try {
            if (consumerState != ConsumerState.WORKING) {
                return false;
            }
            consumerState = ConsumerState.IDLE;
        } finally {
            stateLock.unlock();
        }
        supervisor.markHasJustFinished(); // We changed from working to idle so supervisor must find new task for us
        return true;
    }


    @Override
    public void run() {
        while (alive && !isInterrupted()) {
            try {
                if (consumerState == ConsumerState.WORKING) {
                    processingLock.lock();
                    try {
                        doWork();
                    } finally {
                        processingLock.unlock();
                    }
                } else {
                    stateLock.lock();
                    try {
                        if (consumerState == ConsumerState.SUSPENDED && !firedTimersMemoryStorage.isInSingleConsumerMode()) {
                            if (fetchCursor != null && fetchCursor.isOpen()) {
                                fetchCursor.close(); //if we are in single consumer mode we leave cursor open
                            }
                        }
                        while (alive && consumerState != ConsumerState.WORKING) {
                            sleepCondition.await();
                        }
                    } catch (InterruptedException e) {
                        logger.error(e.getMessage(), e);
                        break;
                    } finally {
                        stateLock.unlock();
                    }
                }
            } catch (Throwable e){
                logger.error("Exception in ConsumerThread", e);
            }
        }
    }

    public void lockProcessing() {
        LockHelper.lockInterruptibly(processingLock);
    }

    public long getInitialLeftBorder() {
        return initialLeftBorder;
    }

    public long getLeftBorder() {
        return leftBorder;
    }

    public long getRightBorder() {
        return rightBorder;
    }

    /* сам поток меняет state только в doWork (под processing lock) в остальном ему state меняет supervisor*/
    public ConsumerState getConsumerState() {
        return consumerState;
    }
    /*
     * use only under  lockProcessing()
     * сами проверяем предварительно что бы consumerState был working
     */

    public void adjustCurrentTaskRightBorder(long newRightBorder) {
        if ((newRightBorder >= rightBorder) || (newRightBorder < leftBorder)) {
            throw new IllegalArgumentException("newRightBorder is invalid");
        }
        this.rightBorder = newRightBorder;
        if ((fetchCursor != null) && (fetchCursor.isOpen())) {
            fetchCursor.setCurrentRightBorder(rightBorder);
        }
    }

    public void unlockProcessing() {
        processingLock.unlock();
    }


    public void setNewTask(long leftBorder, long rightBorder) {
        if (leftBorder > rightBorder) {
            throw new IllegalArgumentException("Border are invalid");
        }
        if (consumerState != ConsumerState.IDLE) {
            throw new IllegalStateException("Consumer must be idle to start new task");
        }
        this.initialLeftBorder = leftBorder;
        this.leftBorder = leftBorder;
        this.rightBorder = rightBorder;
        setState(ConsumerState.WORKING);
    }

    /*
     * return success of operation
     */
    public boolean pauseConsume() {
        LockHelper.lockInterruptibly(stateLock);
        try {
            if (consumerState == ConsumerState.SUSPENDED) { //worker suspended by himself or by supervisor
                return true;
            }
            if (consumerState != ConsumerState.WORKING) { // worker becomes idle by himself
                return false;
            }
            consumerState = ConsumerState.SUSPENDED;
        } finally {
            stateLock.unlock();
        }
        return true;
    }

    public void resumeConsume() {
        if (consumerState != ConsumerState.SUSPENDED) {
            throw new IllegalStateException("Consumer must be suspended to resume");
        }
        setState(ConsumerState.WORKING);
    }


    /*
     * change State without any checks
     */
    private void setState(ConsumerState newState) {
        LockHelper.lockInterruptibly(stateLock);
        try {
            consumerState = newState;
            if (consumerState == ConsumerState.WORKING) {
               sleepCondition.signal();
            }
        } finally {
            stateLock.unlock();
        }
    }


    public void stopAndJoin() {
        LockHelper.lockInterruptibly(stateLock);
        alive = false;
        try {
            sleepCondition.signal();
        } finally {
            stateLock.unlock();
        }

        try {
            this.join();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return getName() + " (" + leftBorder + " , "  + rightBorder + ")";
    }
}
