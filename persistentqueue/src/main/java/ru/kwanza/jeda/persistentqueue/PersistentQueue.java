package ru.kwanza.jeda.persistentqueue;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.api.internal.IQueueObserver;
import ru.kwanza.jeda.api.internal.ITransactionManagerInternal;
import ru.kwanza.jeda.api.internal.SourceException;
import ru.kwanza.jeda.core.queue.*;
import ru.kwanza.jeda.persistentqueue.old.EventWithKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Guzanov Alexander
 */
public class PersistentQueue<E extends IEvent> implements IQueue<E>, IQueueObserver {
    private AbstractTransactionalMemoryQueue<EventWithKey> memoryCache;
    private QueuePersistenceController persistenceController;
    private IQueueObserver originalObserver;
    private IJedaManager manager;
    private QueueObserverChain observer;
    private ReentrantLock putLock = new ReentrantLock();
    private ReentrantLock takeLock = new ReentrantLock();
    private volatile boolean active = false;
    private long maxSize;

    public PersistentQueue(IJedaManager manager, long maxSize, QueuePersistenceController controller) {
        observer = new QueueObserverChain();
        observer.addObserver(this);
        this.manager = manager;
        this.persistenceController = controller;
        this.maxSize = maxSize;
        controller.init(this);
    }

    public IJedaManager getManager() {
        return manager;
    }

    void start() {
        takeLock.lock();
        putLock.lock();
        try {
            memoryCache = createCache(manager, maxSize);
            memoryCache.setObserver(observer);
            ITransactionManagerInternal tm = manager.getTransactionManager();
            tm.begin();
            try {
                //todo aguzanov нужно учесть, что после repair может быть много событий
                Collection<EventWithKey> load = persistenceController.load(-1);
                if (load != null && !load.isEmpty()) {
                    memoryCache.push(load);
                }
                tm.commit();
            } catch (Throwable e) {
                tm.rollback();
                throw new RuntimeException(e);
            }
            active = true;
        } finally {
            putLock.unlock();
            takeLock.unlock();
        }
    }

    void stop() {
        takeLock.lock();
        putLock.lock();
        try {
            active = false;
            memoryCache = null;
        } finally {
            putLock.unlock();
            takeLock.unlock();
        }
    }

    public void setObserver(IQueueObserver observer) {
        if (this.originalObserver != null) {
            this.observer.removeObserver(observer);
        }
        this.originalObserver = observer;

        this.observer.addObserver(originalObserver);
    }

    public IQueueObserver getObserver() {
        return originalObserver;
    }

    public int getEstimatedCount() {
        return active ? memoryCache.getEstimatedCount() : 0;
    }

    public boolean isReady() {
        return memoryCache.isReady();
    }

    public void notifyChange(long queueSize, long delta) {
      // todo aguzanov возможно именно здесь нужно догружать элементы, которые не влезли в память
    }

    public void put(Collection<E> events) throws SinkException {
        if (!manager.getTransactionManager().hasTransaction()) {
            throw new SinkException("Use persistent queue only on transaction!");
        }

        try {
            putLock.lockInterruptibly();
        } catch (InterruptedException e) {
            throw new SinkException.Closed(e);
        }
        try {
            if (!active) {
                throw new SinkException.Closed("Sink closed!");
            }
            ArrayList<EventWithKey> result = new ArrayList<EventWithKey>(events.size());
            for (E e : events) {
                result.add(new EventWithKey(e));
            }
            memoryCache.put(result);
            persistenceController.persist(result);
        } finally {
            putLock.unlock();
        }
    }

    public Collection<E> tryPut(Collection<E> events) throws SinkException {
        if (!manager.getTransactionManager().hasTransaction()) {
            throw new SinkException("Use persistent queue only on transaction!");
        }

        try {
            putLock.lockInterruptibly();
        } catch (InterruptedException e) {
            throw new SinkException.Closed(e);
        }
        try {
            if (!active) {
                throw new SinkException.Closed("Sink closed!");
            }

            ArrayList<EventWithKey> result = new ArrayList<EventWithKey>(events.size());
            for (E e : events) {
                result.add(new EventWithKey(e));
            }
            Collection<EventWithKey> decline = memoryCache.tryPut(result);
            if (decline != null) {
                result.removeAll(decline);
            }
            persistenceController.persist(result);
            return EventWithKey.extract(decline);
        } finally {
            putLock.unlock();
        }
    }

    public Collection<E> take(int count) throws SourceException {
        if (!manager.getTransactionManager().hasTransaction()) {
            throw new SourceException("Use persistent queue only on transaction!");
        }
        try {
            takeLock.lockInterruptibly();
        } catch (InterruptedException e) {
            throw new SourceException(e);
        }
        try {

            if (!active) {
                return null;
            }

            Collection<EventWithKey> result = memoryCache.take(count);
            if (result == null) {
                return null;
            }
            persistenceController.delete(result);

            return EventWithKey.extract(result);
        } finally {
            takeLock.unlock();
        }
    }

    public long size() {
        return active ? memoryCache.size() : 0;
    }

    public boolean isActive() {
        return active;
    }

    protected AbstractTransactionalMemoryQueue<EventWithKey> createCache(IJedaManager manager, long maxSize) {
        return new TransactionalMemoryQueue<EventWithKey>(manager, ObjectCloneType.SERIALIZE, maxSize);
    }


}
