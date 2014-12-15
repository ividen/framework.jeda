package ru.kwanza.jeda.timerservice.pushtimer.consuming.supervisor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kwanza.jeda.timerservice.pushtimer.PendingUpdatesTimeRepository;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClass;
import ru.kwanza.jeda.timerservice.pushtimer.consuming.ConsumerConfig;
import ru.kwanza.jeda.timerservice.pushtimer.consuming.ConsumerState;
import ru.kwanza.jeda.timerservice.pushtimer.consuming.ConsumerThread;
import ru.kwanza.jeda.timerservice.pushtimer.consuming.NodeId;
import ru.kwanza.jeda.timerservice.pushtimer.memory.FiredTimersMemoryStorage;
import ru.kwanza.jeda.timerservice.pushtimer.memory.FiredTimersStorageRepository;
import ru.kwanza.jeda.timerservice.pushtimer.monitoring.EventStatistic;
import ru.kwanza.jeda.timerservice.pushtimer.monitoring.JMXRegistry;
import ru.kwanza.jeda.timerservice.pushtimer.monitoring.mbeans.ConsumerSupervisorMonitoring;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Michael Yeskov
 */
public class ConsumerSupervisorThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerSupervisorThread.class);

    private static final long SLEEP_TIMEOUT = 200;

    private NodeId nodeId;
    private TimerClass timerClass;
    private PendingUpdatesTimeRepository pendingUpdatesTimeRepository;
    private ConsumerSupervisorStageManager consumerSupervisorStageManager;
    private JMXRegistry jmxRegistry;

    private long failoverLeftBorder; //persistent //inclusive
    private long consumerLeftBorder;   //in memory  //inclusive
    private long consumerRightBorder;  // in memory //inclusive
    private long availableRightBorder; //in memory //inclusive

    private FiredTimersMemoryStorage timersMemoryStorage;

    private volatile boolean alive = true;

    private LinkedList<ConsumerThread> idleConsumers = new LinkedList<ConsumerThread>();
    private LinkedList<ConsumerThread> allConsumers = new LinkedList<ConsumerThread>();

    //Working consumers sorted by rightBorder
    private SortedMap<Long, ConsumerThread> workingConsumers = new TreeMap<Long, ConsumerThread>();
    //Suspended consumers sorted by rightBorder
    private SortedMap<Long, ConsumerThread> suspendedByQuotaConsumers = new TreeMap<Long, ConsumerThread>();

    private ConsumerConfig config;

    private String mBeanName;


    //private ConcurrentLinkedQueue<ConsumerThread> justFinishedConsumers = new ConcurrentLinkedQueue<ConsumerThread>();
    private volatile boolean hasJustFinished = false;
    private ReentrantLock hasFinishedLock = new ReentrantLock();
    private Condition hasFinishedCondition = hasFinishedLock.newCondition();


    private ReentrantLock processingLock = new ReentrantLock();


    public ConsumerSupervisorThread(NodeId nodeId, TimerClass timerClass,  PendingUpdatesTimeRepository pendingUpdatesTimeRepository, ConsumerSupervisorStageManager consumerSupervisorStageManager, FiredTimersStorageRepository firedTimersStorageRepository, JMXRegistry jmxRegistry, EventStatistic fetchStats) {
        super("TimerConsumerSupervisor " + nodeId + " " + timerClass);
        this.nodeId = nodeId;
        this.timerClass = timerClass;
        this.pendingUpdatesTimeRepository = pendingUpdatesTimeRepository;
        this.consumerSupervisorStageManager = consumerSupervisorStageManager;
        this.jmxRegistry = jmxRegistry;

        config = timerClass.getConsumerConfig();

        timersMemoryStorage = firedTimersStorageRepository.getFiredTimersStorage(timerClass);


        for (int i = 0; i < config.getWorkerCount(); i++) {
            ConsumerThread newConsumer = new ConsumerThread(i + 1, nodeId, timerClass, this, config, timersMemoryStorage, fetchStats);
            idleConsumers.addLast(newConsumer);
            allConsumers.addLast(newConsumer);
        }

        failoverLeftBorder = consumerSupervisorStageManager.getFailoverLeftBorder(timerClass, nodeId);

        consumerLeftBorder = failoverLeftBorder; //empty interval
        consumerRightBorder = failoverLeftBorder - 1;
        availableRightBorder = failoverLeftBorder - 1;

        mBeanName = jmxRegistry.registerByClass(timerClass.getTimerClassName(), this.getClass().getSimpleName() + "-Node" + nodeId.getEffectiveNodeId(), new ConsumerSupervisorMonitoring(this));
    }

    @Override
    public synchronized void start() {
        for (ConsumerThread consumerThread : allConsumers) {
            consumerThread.start();
        }
        super.start();
    }

    public void stopAndJoin() {
        for (ConsumerThread consumerThread : allConsumers) {
            consumerThread.stopAndJoin();
        }


        try {
            hasFinishedLock.lockInterruptibly();
            alive = false;
            hasFinishedCondition.signal();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            hasFinishedLock.unlock();
        }


        try {
            this.join();
            jmxRegistry.unregister(mBeanName);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void run() {
        while (alive && !isInterrupted()) {
            try {
                hasJustFinished = false;

                processingLock.lock();
                try {
                    doWork();
                }finally {
                    processingLock.unlock();
                }

                try {
                    hasFinishedLock.lockInterruptibly();
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                    break;
                }
                try {
                    long ts = System.currentTimeMillis();
                    long timeout = 0;
                    while (timeout < SLEEP_TIMEOUT && !hasJustFinished && alive) {
                        hasFinishedCondition.await(SLEEP_TIMEOUT - timeout, TimeUnit.MILLISECONDS);
                        timeout = System.currentTimeMillis() - ts;
                    }
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    hasFinishedLock.unlock();
                }
            } catch (Throwable e) {
                logger.error("Exception in ConsumerSupervisorThread", e);
            }
        }
    }


    /*
     * thread safe must be used by ConsumersThread
     */
    public void markHasJustFinished() {
        try {
            hasFinishedLock.lockInterruptibly();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            return;
        }
        try {
            hasJustFinished = true;
            hasFinishedCondition.signal();
        } finally {
            hasFinishedLock.unlock();
        }
    }


    private void doWork() {
        correctAvailableRightBorder();
        correctFailoverLeftBorder();
        processFinishedAndSuspendedWorkers();
        adjustActionsBasedOnMemoryQuota();
    }

    private void correctFailoverLeftBorder() {
        if (failoverLeftBorder < consumerLeftBorder) {
            long candidate = Math.min(consumerLeftBorder, timersMemoryStorage.getMinPendingExpire(nodeId.getEffectiveNodeId()));
            if (candidate > failoverLeftBorder) {
               consumerSupervisorStageManager.updateFailoverLeftBorder(timerClass, nodeId, candidate);
               failoverLeftBorder = candidate;
            }
        }

    }

    private void processFinishedAndSuspendedWorkers() {
        Iterator<ConsumerThread> iterator = workingConsumers.values().iterator();
        while (iterator.hasNext()) {
            ConsumerThread consumer = iterator.next();
            ConsumerState currentState = consumer.getConsumerState();
            if (currentState != ConsumerState.WORKING) {
                iterator.remove();
                if (currentState == ConsumerState.IDLE) {
                    idleConsumers.add(consumer);
                }
                if (currentState == ConsumerState.SUSPENDED) {
                    suspendedByQuotaConsumers.put(consumer.getRightBorder(), consumer);
                }
            }
        }
        long consumerLeftBorderCandidate = consumerRightBorder + 1;
        if (!workingConsumers.isEmpty()) {
            consumerLeftBorderCandidate = Math.min(consumerLeftBorderCandidate,
                    workingConsumers.get(workingConsumers.firstKey()).getInitialLeftBorder());
        }
        if (!suspendedByQuotaConsumers.isEmpty()) {
            consumerLeftBorderCandidate = Math.min(consumerLeftBorderCandidate,
                    suspendedByQuotaConsumers.get(suspendedByQuotaConsumers.firstKey()).getInitialLeftBorder());
        }
        if (consumerLeftBorderCandidate > consumerLeftBorder) {
            consumerLeftBorder = consumerLeftBorderCandidate;
        }
    }


    private void correctAvailableRightBorder() {
        long availableRightBorderCandidate = pendingUpdatesTimeRepository.getSafeToConsumeRightBorder(timerClass, nodeId.isInRepairMode());
        if (availableRightBorderCandidate - consumerRightBorder >= timerClass.getConsumerConfig().getBorderGain()) {
            availableRightBorder = availableRightBorderCandidate;
        }
    }

    private void adjustActionsBasedOnMemoryQuota() {
        if (timersMemoryStorage.isInSingleConsumerMode()) { //check once current memory state
            if (workingConsumers.size() > 1) {
                //just entered single consumer mode need to suspend some consumers
                massSuspendConsumers();

            } else if (workingConsumers.size() == 0) { //in single consumerMode with no consumers

                if (suspendedByQuotaConsumers.size() > 0) { //use suspended
                    ConsumerThread candidate = suspendedByQuotaConsumers.get(suspendedByQuotaConsumers.firstKey());
                    suspendedByQuotaConsumers.remove(candidate.getRightBorder());

                    workingConsumers.put(candidate.getRightBorder(), candidate);
                    candidate.resumeConsume();
                } else if (idleConsumers.size() > 0) { //use idle
                    expandConsumers(true);
                } else {
                    //log error no consumers were configured
                }

            } //if we are in single consumer mode with one working consumer than everything is ok

        } else { //MULTI consumer mode
            if (suspendedByQuotaConsumers.size() > 0) {
                massResumeConsumers();
            }
            expandConsumers(false);
        }

    }

    private void massResumeConsumers() {
        Iterator<ConsumerThread> iterator = suspendedByQuotaConsumers.values().iterator();
        while (iterator.hasNext()) {
            ConsumerThread consumer = iterator.next();
            workingConsumers.put(consumer.getRightBorder(), consumer);
            iterator.remove();
            consumer.resumeConsume();  //important to resume after we call getRightBorders not before
        }

    }

    private void massSuspendConsumers() {
        Iterator<ConsumerThread> iterator = workingConsumers.values().iterator();
        iterator.next(); //skip first element it will continue to work
        while (iterator.hasNext()) {
            ConsumerThread consumer = iterator.next();
            if (consumer.pauseConsume()) { //suspend successfully
                suspendedByQuotaConsumers.put(consumer.getRightBorder(), consumer);
            } else { //unsuccessful suspend only if worker already stops
                idleConsumers.add(consumer);
            }
            iterator.remove();
        }
    }

    private void expandConsumers(boolean singleConsumerMode) {

        if (consumerRightBorder - consumerLeftBorder < config.getIdealWorkingInterval()) { //need to expand
            if (consumerRightBorder < availableRightBorder) { //can expand
                long oldIntervalEffectiveLeft = Math.min(consumerLeftBorder, consumerRightBorder); //left can be more than right when interval is empty

                long expandRightBorder = availableRightBorder;

                if (!workingConsumers.isEmpty()) { //if we have at least one consumer then apply "ideal interval" rule
                    expandRightBorder = Math.min(oldIntervalEffectiveLeft + config.getIdealWorkingInterval() ,  expandRightBorder);
                }



                if (!idleConsumers.isEmpty()) { //in single consumer mode  ===  !IsEmpty
                    ConsumerThread candidate = idleConsumers.pop();
                    candidate.setNewTask(consumerRightBorder + 1, expandRightBorder);
                    workingConsumers.put(candidate.getRightBorder(), candidate);
                    consumerRightBorder = expandRightBorder;
                    //if current workers count = 0 then left border is already oldRight + 1
                }

            } else {
                return; //nothing to do we will try in next tick
            }
        } else if (!singleConsumerMode) { //Expand is over, need to speed up processing
            addHelperConsumer();
        }

    }

    /*
     * (add helping consumer to oldest consumer)
     * (using only in multi consumer mode)
     */
    private void addHelperConsumer() {
        if (!idleConsumers.isEmpty()) {
            ConsumerThread needHelpConsumer = workingConsumers.get(workingConsumers.firstKey());
            try {
                needHelpConsumer.lockProcessing();
                long currentLeft = needHelpConsumer.getLeftBorder();
                long currentRight = needHelpConsumer.getRightBorder();
                if ((needHelpConsumer.getConsumerState() == ConsumerState.WORKING) && (currentRight - currentLeft > 1)) { //todo: возможно стоит использвать минимальный порог по которому не добавляем helper
                    workingConsumers.remove(needHelpConsumer.getRightBorder());
                    long length = (currentRight - currentLeft) / 2;
                    needHelpConsumer.adjustCurrentTaskRightBorder(currentLeft + length);
                    workingConsumers.put(needHelpConsumer.getRightBorder(), needHelpConsumer);


                    ConsumerThread helperConsumer = idleConsumers.pop();
                    helperConsumer.setNewTask(currentLeft + length + 1, currentRight);
                    workingConsumers.put(helperConsumer.getRightBorder(), helperConsumer);
                }
            } finally {
                needHelpConsumer.unlockProcessing();
            }

        }
    }

    /*locking for monitoring use carefully*/

    public void lockProcessing(){
        processingLock.lock();
    }

    public void unlockProcessing(){
        processingLock.unlock();
    }

    /*getters for monitoring */

    public NodeId getNodeId() {
        return nodeId;
    }

    public TimerClass getTimerClass() {
        return timerClass;
    }

    public static long getSleepTimeout() {
        return SLEEP_TIMEOUT;
    }

    public ConsumerConfig getConfig() {
        return config;
    }

    public long getFailoverLeftBorder() {
        return failoverLeftBorder;
    }

    public long getConsumerLeftBorder() {
        return consumerLeftBorder;
    }

    public long getConsumerRightBorder() {
        return consumerRightBorder;
    }

    public long getAvailableRightBorder() {
        return availableRightBorder;
    }

    public LinkedList<ConsumerThread> getAllConsumers() {
        return allConsumers;
    }

    public LinkedList<ConsumerThread> getIdleConsumers() {
        return idleConsumers;
    }

    public SortedMap<Long, ConsumerThread> getWorkingConsumers() {
        return workingConsumers;
    }

    public SortedMap<Long, ConsumerThread> getSuspendedByQuotaConsumers() {
        return suspendedByQuotaConsumers;
    }

    public boolean getAliveValue() {
        return alive;
    }

    public boolean isThreadAlive() {
        return isAlive();
    }

    public boolean isThreadInterrupted() {
        return isInterrupted();
    }

}
