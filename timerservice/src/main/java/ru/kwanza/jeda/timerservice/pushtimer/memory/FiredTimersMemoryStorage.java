package ru.kwanza.jeda.timerservice.pushtimer.memory;

import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.api.internal.IJedaManagerInternal;
import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.api.internal.IStageInternal;
import ru.kwanza.jeda.core.manager.ObjectNotFoundException;
import ru.kwanza.jeda.timerservice.pushtimer.LockHelper;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClass;
import ru.kwanza.jeda.timerservice.pushtimer.TimerEntity;
import ru.kwanza.jeda.timerservice.pushtimer.internalapi.InternalTimerFiredEvent;
import ru.kwanza.jeda.timerservice.pushtimer.monitoring.JMXRegistry;
import ru.kwanza.jeda.timerservice.pushtimer.monitoring.mbeans.FiredTimersMemoryStorageMonitoring;
import ru.kwanza.jeda.timerservice.pushtimer.processor.ExpireTimeProcessor;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * must be thread safe
 * ConsumerThread will use it concurrently
 * In case of node fail several ConsumerSupervisorThread will use one instance of FiredTimersMemoryStorage
 * @author Michael Yeskov
 */
public class FiredTimersMemoryStorage {


    private TimerClass timerClass;
    private IJedaManagerInternal jedaManager;
    private FiredTimersStorageRepository repository;
    private JMXRegistry jmxRegistry;

    private long maxLimit;
    private long singleConsumerModeLimit;
    private long againMultiConsumerModeBorder;

    private volatile boolean isInSingleConsumerMode = false;

    private volatile long reservedInserts = 0;

    private Map<Long, SortedMap<Long, Long>> bucketIdToPendingSortedByExpiry = new HashMap<Long, SortedMap<Long, Long>>();

    private Map<String, IQueue> timerNameToQueue = new HashMap<String, IQueue>();

    private ReentrantLock lock = new ReentrantLock(); //potentially "long lock" - using lockInterruptibly (working with transaction manager under lock)

    private boolean dirty = true;

    public FiredTimersMemoryStorage(TimerClass timerClass, IJedaManagerInternal jedaManager, FiredTimersStorageRepository repository, JMXRegistry jmxRegistry) {
        this.timerClass = timerClass;
        this.jedaManager = jedaManager;
        this.repository = repository;
        this.jmxRegistry = jmxRegistry;

        maxLimit = timerClass.getConsumerConfig().getFiredTimersMaxLimit();
        singleConsumerModeLimit = timerClass.getConsumerConfig().getFiredTimersSingleConsumerModeLimit();
        againMultiConsumerModeBorder = timerClass.getConsumerConfig().getFiredTimersAgainMultiConsumerBorder();

        jmxRegistry.registerByClass(timerClass.getTimerClassName(), this.getClass().getSimpleName(), new FiredTimersMemoryStorageMonitoring(this));
    }

    private void configError(String timerName, String msg) {
        throw new RuntimeException("Timer consuming Stage('"+ timerName +"') "+ msg);
    }

    public boolean isInSingleConsumerMode() {
        LockHelper.lockInterruptibly(lock);

        try {
            long currentSize = getCurrentSize();
            if (!isInSingleConsumerMode && currentSize > singleConsumerModeLimit) {
                isInSingleConsumerMode = true;
            } else if (isInSingleConsumerMode && currentSize < againMultiConsumerModeBorder) {
                isInSingleConsumerMode = false;
            }
            return isInSingleConsumerMode;
        } finally {
            lock.unlock();
        }
    }

    private long getCurrentSize() {
        checkDirty();
        long currentSize = 0;
        for (IQueue queue : timerNameToQueue.values()) {
            currentSize += queue.size();
        }
        return currentSize;
    }



    public boolean canAccept(long fetchSize) {
        LockHelper.lockInterruptibly(lock);
        try {
            long currentSize = getCurrentSize();
            if (currentSize + fetchSize + reservedInserts <= maxLimit) {
                reservedInserts += fetchSize;
                return true;
            } else {
                return false;
            }
        } finally {
            lock.unlock();
        }
    }

    public void forgetCanAccept(long remainingCount) {
        if (remainingCount <= 0) {
            return;
        }
        LockHelper.lockInterruptibly(lock);
        try {
            if (reservedInserts - remainingCount < 0 ) {
                throw new RuntimeException("You must run canAccept first");
            }
            reservedInserts -= remainingCount;
        } finally {
            lock.unlock();
        }

    }

    public void accept(List<TimerEntity> firedTimers, long bucketId) {
        List<InternalTimerFiredEvent> allEvents = new ArrayList<InternalTimerFiredEvent>();
        Map<String, List<InternalTimerFiredEvent>> timerNameToEvents = new HashMap<String, List<InternalTimerFiredEvent>>();
        splitByNameAndFill(allEvents, timerNameToEvents, firedTimers, bucketId);


        LockHelper.lockInterruptibly(lock);

        boolean pendingAddSuccess = false;
        try {
            checkDirty();
            pendingAddSuccess = addPendingTimers(allEvents, bucketId);
            jedaManager.getTransactionManager().begin();

            for (Map.Entry<String, List<InternalTimerFiredEvent>> entry : timerNameToEvents.entrySet()) {
                IQueue queue = timerNameToQueue.get(entry.getKey());
                try {
                    queue.put(entry.getValue());
                } catch (SinkException e) {
                    throw new RuntimeException("Can't put fired timers in memory for TimerName=" + entry.getKey(), e);
                }
            }

            jedaManager.getTransactionManager().commit();
            if (firedTimers.size() > reservedInserts) {
                throw new RuntimeException(firedTimers.size() + " is more than reservedInserts = " + reservedInserts);
            }
            reservedInserts -= firedTimers.size();
        } catch (Throwable e) {
            try {
                if (jedaManager.getTransactionManager().hasTransaction()) {
                    jedaManager.getTransactionManager().rollback();
                }
            } catch (Throwable e1) {
                //ignore
            }
            if (pendingAddSuccess) {
                forgetPendingTimers(allEvents, bucketId);
            }
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    private void splitByNameAndFill(List<InternalTimerFiredEvent> allEvents,
                                    Map<String, List<InternalTimerFiredEvent>> timerNameToEvents,
                                    List<TimerEntity> firedTimers, long bucketId) {
        long nodeStartupPointCount = repository.getBucketStartupPointCount().get(bucketId);
        for (TimerEntity timerEntity : firedTimers) {
            List<InternalTimerFiredEvent> currentNameEvents = timerNameToEvents.get(timerEntity.getTimerName());
            if (currentNameEvents == null) {
                currentNameEvents = new ArrayList<InternalTimerFiredEvent>();
                timerNameToEvents.put(timerEntity.getTimerName(), currentNameEvents);
            }
            InternalTimerFiredEvent current = new InternalTimerFiredEvent(timerEntity.getTimerName(), timerEntity.getTimerId(), bucketId, timerEntity.getExpireTime(), nodeStartupPointCount);
            currentNameEvents.add(current);
            allEvents.add(current);
        }
    }


    public void forgetPendingTimers(Map<Long, List<InternalTimerFiredEvent>> bucketIdToEvents)  {

        LockHelper.lockInterruptibly(lock);
        try {
            for (Map.Entry<Long, List<InternalTimerFiredEvent>> entry : bucketIdToEvents.entrySet()) {
                forgetPendingTimers(entry.getValue(), entry.getKey());
            }
        } finally {
            lock.unlock();
        }
    }


    //use under lock
    private SortedMap<Long, Long> getPendingMap(long bucketId) {
        SortedMap<Long, Long> pendingMap = bucketIdToPendingSortedByExpiry.get(bucketId);
        if (pendingMap == null) {
            pendingMap = new TreeMap<Long, Long>();
            bucketIdToPendingSortedByExpiry.put(bucketId, pendingMap);
        }
        return pendingMap;
    }

    //use under lock
    private void forgetPendingTimers(List<InternalTimerFiredEvent> firedTimers, long bucketId) {
        SortedMap<Long, Long> pendingMap = getPendingMap(bucketId);
        for (InternalTimerFiredEvent timerEvent : firedTimers) {
            Long currentCount = pendingMap.get(timerEvent.getExpireTime());
            if (currentCount == null || currentCount == 0) {
                throw new IllegalStateException("No timer with this expire was registered");
            }
            if (currentCount == 1) {
                pendingMap.remove(timerEvent.getExpireTime());
            } else {
                pendingMap.put(timerEvent.getExpireTime(), currentCount - 1);
            }
        }


    }

    //use under lock
    private boolean addPendingTimers(List<InternalTimerFiredEvent> firedTimers, long bucketId) {
        SortedMap<Long, Long> pendingMap = getPendingMap(bucketId);

        for (InternalTimerFiredEvent timerEvent : firedTimers) {
            Long currentCount = pendingMap.get(timerEvent.getExpireTime());
            if (currentCount == null) {
                currentCount = 0L;
            }
            pendingMap.put(timerEvent.getExpireTime(), currentCount + 1);
        }

        return true;

    }


    public long getMinPendingExpire(Integer bucketId) {
        LockHelper.lockInterruptibly(lock);
        try {
            SortedMap<Long, Long> pendingMap  = getPendingMap(bucketId);
            if (pendingMap.isEmpty()){
                return Long.MAX_VALUE;
            }
            return  pendingMap.firstKey();
        } finally {
            lock.unlock();
        }
    }

    public void clearPendingTimers(long bucketId) {
        LockHelper.lockInterruptibly(lock);
        try {
            getPendingMap(bucketId).clear();
        } finally {
            lock.unlock();
        }
    }

    //use under lock
    private void checkDirty() {
        if (dirty == false) {
            return;
        }
        for (String timerName : timerClass.getCompatibleTimerNames()){
            IStageInternal consumingStage;
            try {
                consumingStage = jedaManager.getStageInternal(timerName);
                if (consumingStage.getAdmissionController() != null) {
                    configError(timerName, "can't have AdmissionController");
                }
                if (!(consumingStage.getProcessor() instanceof ExpireTimeProcessor)) {
                    configError(timerName, "must have ExpireTimeProcessor");
                }
                if (!consumingStage.hasTransaction()) {
                    configError(timerName, "Stage must be transactional");
                }

                /*
                AbstractTransactionalMemoryQueue queue = (AbstractTransactionalMemoryQueue)consumingStage.getQueue();
                if (queue.getMaxSize() < maxLimit ) {
                    configError(timerName, "Queue Max Size must be not less than firedTimersMaxLimit=" + maxLimit);
                }
                */

                timerNameToQueue.put(timerName, consumingStage.getQueue());
            } catch (ObjectNotFoundException e) {
                throw new RuntimeException("You must define Stage name equals to TimerName for expire event processing. TimerName ='" + timerName +"'", e);
            }
        }
        dirty = false;
    }

    /** external locking use carefully (intended for jmx) **/
    public void lock(){
        LockHelper.lockInterruptibly(lock);
    }

    public void unlock(){
        lock.unlock();
    }

    public long getMaxLimit() {
        return maxLimit;
    }

    public long getSingleConsumerModeLimit() {
        return singleConsumerModeLimit;
    }

    public long getAgainMultiConsumerModeBorder() {
        return againMultiConsumerModeBorder;
    }

    public long getReservedInserts() {
        return reservedInserts;
    }

    public boolean getCurrentSingleConsumerMode(){
        return isInSingleConsumerMode;
    }

    public Map<String, IQueue> getTimerNameToQueue() {
        return timerNameToQueue;
    }

    public Map<Long, SortedMap<Long, Long>> getBucketIdToPendingSortedByExpiry() {
        return bucketIdToPendingSortedByExpiry;
    }
}
