package ru.kwanza.jeda.core.queue;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.api.internal.SourceException;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Guzanov Alexander
 */
public abstract class AbstractMemoryQueue<E extends IEvent> extends AbstractObservableMemoryQueue<E> {
    protected int maxSize;
    private ReentrantLock putLock = new ReentrantLock();
    private ReentrantLock takeLock = new ReentrantLock();
    private AtomicInteger size = new AtomicInteger(0);

    public AbstractMemoryQueue(int maxSize) {
        this.maxSize = maxSize;
    }

    protected abstract void addToTail(E e);

    protected abstract Collection<E> doTake(int c);

    public int getEstimatedCount() {
        return size.get();
    }

    public void put(Collection<E> events) throws SinkException {
        if (events.isEmpty()) {
            return;
        }
        putLock();
        try {
            int s = size.get();
            if (events.size() + s > maxSize) {
                throw new SinkException.Clogged("Sink maxSize=" + maxSize
                        + ",currentSize=" + s + ", try to put " + events.size() + " elements!");
            }
            for (E e : events) {
                addToTail(e);
                size.incrementAndGet();
            }
            notify(size.get(), events.size());
        } finally {
            putUnlock();
        }
    }

    public Collection<E> tryPut(Collection<E> events) throws SinkException {
        if (events.isEmpty()) {
            return null;
        }
        putLock();
        try {
            MutableEventCollection result = new MutableEventCollection();
            for (E e : events) {
                if (size.get() < maxSize) {
                    addToTail(e);
                    size.incrementAndGet();
                } else {
                    result.add(e);
                }
            }
            notify(size.get(), events.size() - result.size());

            if (result.size() == 0) return null;

            return result;
        } finally {
            putUnlock();
        }
    }

    public Collection<E> take(int count) throws SourceException {
        if (count == 0) {
            return null;
        }
        takeLock();
        try {
            if (size.get() == 0) {
                return null;
            }

            int c = Math.min(size.get(), count);
            Collection<E> result = doTake(c);

            int value = size.addAndGet(-c);
            notify(value, -c);
            return result;
        } finally {
            takeUnlock();
        }
    }

    public int size() {
        return size.get();
    }

    public boolean isReady() {
        return size() > 0;
    }

    public int getMaxSize() {
        return maxSize;
    }

    private void putLock() throws SinkException.Closed {
        try {
            putLock.lockInterruptibly();
        } catch (InterruptedException e) {
            throw new SinkException.Closed(e);
        }
    }

    private void putUnlock() {
        putLock.unlock();
    }

    private void takeLock() throws SourceException {
        try {
            takeLock.lockInterruptibly();
        } catch (InterruptedException e) {
            throw new SourceException(e);
        }
    }

    private void takeUnlock() {
        takeLock.unlock();
    }
}
