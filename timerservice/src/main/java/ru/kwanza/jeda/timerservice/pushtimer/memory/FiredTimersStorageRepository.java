package ru.kwanza.jeda.timerservice.pushtimer.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import ru.kwanza.jeda.api.internal.IJedaManagerInternal;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClass;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClassRepository;
import ru.kwanza.jeda.timerservice.pushtimer.monitoring.JMXRegistry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Michael Yeskov
 */
@Repository
public class FiredTimersStorageRepository{

    private static final Logger logger = LoggerFactory.getLogger(FiredTimersStorageRepository.class);

    @Resource
    private TimerClassRepository timerClassRepository;
    @Resource (name = "jeda.IJedaManager")
    private IJedaManagerInternal jedaManager;
    @Resource
    private JMXRegistry jmxRegistry;

    private Map<TimerClass, FiredTimersMemoryStorage> classToStorage = new HashMap<TimerClass, FiredTimersMemoryStorage>();


    private ReentrantLock lock = new ReentrantLock();
    private Condition waitForActiveCompletion = lock.newCondition();

    private Set<Long> processingStoppedBucketIds = new HashSet<Long>();
    private Map<Long, AtomicLong> bucketActiveProcessCount = new HashMap<Long, AtomicLong>();

    private ConcurrentHashMap<Long, Long> bucketStartupPointCount = new ConcurrentHashMap<Long, Long>();


    @PostConstruct
    public void init() {
        for (TimerClass timerClass :  timerClassRepository.getRegisteredTimerClasses()) {
            classToStorage.put(timerClass, new FiredTimersMemoryStorage(timerClass, jedaManager, this, jmxRegistry));
        }
    }


    public FiredTimersMemoryStorage getFiredTimersStorage(TimerClass timerClass) {
        return classToStorage.get(timerClass);
    }

    public Map<Long, Long> registerActiveProcessor(Set<Long> bucketIds) {
        Map<Long, Long> resultBucketIdToPointCount = new HashMap<Long, Long>();
        lock.lock(); //interrupt??
        try {
            for (long bucketId : bucketIds) {
                if (processingStoppedBucketIds.contains(bucketId)) {
                    continue;
                }

                AtomicLong count = bucketActiveProcessCount.get(bucketId);
                if (count == null) {
                    count = new AtomicLong(0);
                    bucketActiveProcessCount.put(bucketId, count);
                }
                count.incrementAndGet();


                long startupPointCount = bucketStartupPointCount.get(bucketId);
                resultBucketIdToPointCount.put(bucketId, startupPointCount);
            }
        } finally {
            lock.unlock();
        }

        return resultBucketIdToPointCount;
    }

    public void forgetActiveProcessors(Set<Long> bucketIdToForget) {
        lock.lock(); //interrupt??
        try {
            for (Long bucketId : bucketIdToForget) {
                AtomicLong count = bucketActiveProcessCount.get(bucketId);
                if (count == null || count.get() == 0) {
                    //log silently return
                    return;
                }
                long value = count.decrementAndGet();
                if (value == 0) {
                    waitForActiveCompletion.signal();
                }
            }
        } finally {
            lock.unlock();
        }

    }

    public void stopProcessingAndWait(long bucketId) {
        lock.lock(); //interrupt??
        try{
            processingStoppedBucketIds.add(bucketId);
            try {
                while (true) {
                    AtomicLong count = bucketActiveProcessCount.get(bucketId);
                    if (count == null || count.get() == 0) {
                        break;
                    }
                    waitForActiveCompletion.await(1000, TimeUnit.MILLISECONDS);
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
                return; //TODO: implement right
            }
            for (FiredTimersMemoryStorage storage : classToStorage.values()) {
                storage.clearPendingTimers(bucketId);
            }
        } finally {
            lock.unlock();
        }
    }

    public void resumeProcessing(long bucketId, long startupPointCount) {
        lock.lock(); //interrupt??
        try{
            processingStoppedBucketIds.remove(bucketId);
            bucketStartupPointCount.put(bucketId, startupPointCount);
        } finally {
            lock.unlock();
        }
    }

    public ConcurrentHashMap<Long, Long> getBucketStartupPointCount() {
        return bucketStartupPointCount;
    }

    /*
    @Override
    public int getPhase() {
        return 10;
    }
    */

    /*use carefully , for jmx*/
    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public Set<Long> getProcessingStoppedBucketIds() {
        return processingStoppedBucketIds;
    }

    public Map<Long, AtomicLong> getBucketActiveProcessCount() {
        return bucketActiveProcessCount;
    }
}
