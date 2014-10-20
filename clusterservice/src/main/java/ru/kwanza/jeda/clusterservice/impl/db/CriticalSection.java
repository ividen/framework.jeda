package ru.kwanza.jeda.clusterservice.impl.db;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Alexander Guzanov
 */
class CriticalSection {
    private volatile boolean startCritical = false;
    private ReentrantLock criticalLock = new ReentrantLock();
    private Condition isStartCritical = criticalLock.newCondition();
    private volatile long finishBarrier = 0;
    private Condition isFinishCritical = criticalLock.newCondition();


    public <R> R execute(Callable<R> callable) throws InterruptedException, InvocationTargetException {
        try {
            return execute(callable, 0, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new InvocationTargetException(e);
        }
    }


    public <R> R execute(Callable<R> callable, long ts, TimeUnit unit)
            throws InterruptedException, InvocationTargetException, TimeoutException {
        waitForStart(ts, unit);
        try {
            return callable.call();
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        } finally {
            signalFinish();
        }
    }

    private void waitForStart(long ts, TimeUnit unit) throws InterruptedException, TimeoutException {
        criticalLock.lock();
        try {
            if (!await(ts, unit)) {
                throw new TimeoutException();
            }
            finishBarrier = finishBarrier + 1;
        } finally {
            criticalLock.unlock();
        }
    }

    private boolean await(long ts, TimeUnit unit) throws InterruptedException {
        if (startCritical){
            return true;
        }

        long finishTs = System.currentTimeMillis() + unit.toMillis(ts);
        do {
            if (ts > 0) {
                if (isStartCritical.await(ts, unit) && startCritical)
                    return true;
            } else {
                isStartCritical.await();
            }
        }

        while (System.currentTimeMillis() < finishTs);

        return startCritical;
    }

    public void signalFinish() {
        criticalLock.lock();
        try {
            finishBarrier = finishBarrier - 1;
            if (finishBarrier <= 0) {
                isFinishCritical.signalAll();
            }
        } finally {
            criticalLock.unlock();
        }

    }

    public void enter() {
        criticalLock.lock();
        try {
            startCritical = true;
            isStartCritical.signalAll();
        } finally {
            criticalLock.unlock();
        }
    }

    public void exit() {
        criticalLock.lock();
        try {
            while (finishBarrier > 0) {
                try {
                    isFinishCritical.await();
                } catch (InterruptedException e) {
                }
            }
        } finally {
            startCritical = false;
            criticalLock.unlock();
        }
    }
}
