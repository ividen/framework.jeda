package ru.kwanza.jeda.timerservice.pushtimer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import ru.kwanza.jeda.api.pushtimer.manager.NewTimer;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClass;
import ru.kwanza.jeda.timerservice.pushtimer.tx.Tx;


import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * работаем в рамках timer class конкурентно
 * @author Michael Yeskov
 */
@Repository
public class PendingUpdatesTimeRepository {
    private static final Logger logger = LoggerFactory.getLogger(PendingUpdatesTimeRepository.class);

    private ReentrantLock lock = new ReentrantLock();
    private Map<Tx, Map<TimerClass, Long>> minPendingExpiry = new HashMap<Tx, Map<TimerClass, Long>>();
    private Map<TimerClass, SortedMap<Long, AtomicLong>> expiryToTimerCount = new HashMap<TimerClass, SortedMap<Long, AtomicLong>>();

    /*
     * must not throw exception be atomic
     */
    public Collection<TimerEntity> registerTimers(TimerClass timerClass, Collection<NewTimer> timers, Tx tx) {
        if (timers.isEmpty()) { //nothing to do
            return new ArrayList<TimerEntity>();
        }

        lock.lock();
        try {
            Map<TimerClass, Long> currentTxMinPendExp =  minPendingExpiry.get(tx);
            if (currentTxMinPendExp == null) {
                currentTxMinPendExp = new HashMap<TimerClass, Long>();
                minPendingExpiry.put(tx, currentTxMinPendExp);
            }

            Collection<TimerEntity> result = toTimerEntity(timers, System.currentTimeMillis());

            Long minExpiry = currentTxMinPendExp.get(timerClass);
            long minCandidate = findMin(result);

            if (minExpiry == null || minExpiry > minCandidate) {
                if (minExpiry != null) {
                    decCount(timerClass, minExpiry);
                }
                currentTxMinPendExp.put(timerClass, minCandidate);
                incCount(timerClass, minCandidate);
            }

            return result;
        } finally {
            lock.unlock();
        }
    }

    private long findMin(Collection<TimerEntity> source) {
        long min = Long.MAX_VALUE;
        for (TimerEntity current : source) {
            if (current.getExpireTime() < min) {
                min = current.getExpireTime();
            }
        }
        if (min == Long.MAX_VALUE) {
            throw new RuntimeException("Collection can't be empty");
        }
        return min;
    }

    private List<TimerEntity> toTimerEntity(Collection<NewTimer> timers, long now) {
        List<TimerEntity> result  = new ArrayList<TimerEntity>();
        for (NewTimer current : timers) {
            TimerEntity entity = new TimerEntity(current.getTimerName(), current.getTimerId(), now + current.getTimeoutMS());
            result.add(entity);
        }
        return result;
    }


    private void decCount(TimerClass timerClass, Long expiry) {
        AtomicLong count = expiryToTimerCount.get(timerClass).get(expiry);
        if (count.decrementAndGet() == 0){
            expiryToTimerCount.get(timerClass).remove(expiry);
        }
    }

    private void incCount(TimerClass timerClass, long expiry) {
        SortedMap<Long, AtomicLong> currentClassMap  = expiryToTimerCount.get(timerClass);
        if (currentClassMap == null) {
            currentClassMap = new TreeMap<Long, AtomicLong>();
            expiryToTimerCount.put(timerClass, currentClassMap);
        }

        AtomicLong count = currentClassMap.get(expiry);
        if (count == null) {
            count = new AtomicLong(0);
            currentClassMap.put(expiry, count);
        }

        count.incrementAndGet();
    }

    /*
     * must not throw exception
     * if tx doesn't exists just ignore it
     *
     */
    public void removeTimers(Tx tx) {
        lock.lock();
        try {
            Map<TimerClass, Long> currentTxMap = minPendingExpiry.get(tx);
            if (currentTxMap == null){
                logger.debug("RemoveTimers was called for non existing Tx");
                return;
            }
            for (Map.Entry<TimerClass, Long> entry : currentTxMap.entrySet()) {
                decCount(entry.getKey(), entry.getValue());
            }
            minPendingExpiry.remove(tx);
        } finally {
            lock.unlock();
        }
    }

    public long getSafeToConsumeRightBorder(TimerClass timerClass, boolean isReadonlyStorage) {
        if (isReadonlyStorage) {
            return System.currentTimeMillis();
        } else {
            lock.lock();
            try {
                long minPending = Long.MAX_VALUE;
                SortedMap<Long, AtomicLong> currentMap = expiryToTimerCount.get(timerClass);
                if (currentMap != null && !currentMap.isEmpty()) {
                    minPending = currentMap.firstKey();
                }

                return Math.min(System.currentTimeMillis() - 1, minPending - 1);
            } finally {
                lock.unlock();
            }
        }

    }


    /*use carefully, for jmx*/
    public void lock(){
        lock.lock();
    }

    public void unlock(){
        lock.unlock();
    }

    public Map<Tx, Map<TimerClass, Long>> getMinPendingExpiry() {
        return minPendingExpiry;
    }

    public Map<TimerClass, SortedMap<Long, AtomicLong>> getExpiryToTimerCount() {
        return expiryToTimerCount;
    }
}
