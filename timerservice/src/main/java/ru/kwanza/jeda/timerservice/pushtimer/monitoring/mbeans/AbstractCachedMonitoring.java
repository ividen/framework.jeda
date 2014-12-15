package ru.kwanza.jeda.timerservice.pushtimer.monitoring.mbeans;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Michael Yeskov
 */
public abstract class AbstractCachedMonitoring {
    public static final long CACHE_UPDATE_INTERVAL = 1000;
    protected ReentrantLock lock = new ReentrantLock();
    protected volatile long cacheUpdateTime = 0;


    protected  <T> T getFromCache(Callable<T> callable) {
        lock.lock();
        try {
            updateCache();
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } finally {
            lock.unlock();
        }
    }


    protected void updateCache() {
        long requestTime = System.currentTimeMillis();

        if (requestTime - cacheUpdateTime >= CACHE_UPDATE_INTERVAL) {
            fillCache();
            cacheUpdateTime = System.currentTimeMillis();
        }
    }

    protected abstract void fillCache();

}
