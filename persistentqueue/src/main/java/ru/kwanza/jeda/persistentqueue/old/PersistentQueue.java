package ru.kwanza.jeda.persistentqueue.old;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.api.internal.*;
import ru.kwanza.jeda.clusterservice.old.ClusterService;
import ru.kwanza.jeda.clusterservice.old.INodeListener;
import ru.kwanza.jeda.core.queue.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * todo aguzanov продумать возможность блокировки встаки в очередь при  ожидании трансверинга
 *
 * @author Guzanov Alexander
 */
public class PersistentQueue<E extends IEvent> implements IQueue<E>, INodeListener, IQueueObserver {
    private AbstractTransactionalMemoryQueue<EventWithKey> memoryCache;
    private AtomicInteger maxTransferCount = new AtomicInteger(0);
    private IQueuePersistenceController persistenceController;
    private IQueueObserver originalObserver;
    private IJedaManager manager;
    private QueueObserverChain observer;
    private ReentrantLock putLock = new ReentrantLock();
    private ReentrantLock takeLock = new ReentrantLock();
    private ReentrantLock transferLock = new ReentrantLock();
    private Condition retransmitCondition = transferLock.newCondition();
    private volatile boolean active = false;
    private volatile long waitingForTransfer = 0;
    private int maxSize;

    public PersistentQueue(IJedaManager manager, int maxSize, IQueuePersistenceController controller) {
        observer = new QueueObserverChain();
        observer.addObserver(this);
        this.manager = manager;
        this.persistenceController = controller;
        this.maxSize = maxSize;
        ClusterService.subscribe(this);
    }

    public IJedaManager getManager() {
        return manager;
    }

    public void onNodeLost(Long nodeId, long lastNodeTs) {
        putLock.lock();
        takeLock.lock();
        transferLock.lock();
        try {
            waitingForTransfer++;
            if (maxTransferCount.get() == 0) {
                int delta = memoryCache.getMaxSize() - getEstimatedCount();
                maxTransferCount.set(delta);
            }
            QueueEventsTransfer.getInstance().schedule(this, nodeId, lastNodeTs);
        } finally {
            transferLock.unlock();
            takeLock.unlock();
            putLock.unlock();
        }
    }

    public void onNodeActivate(Long nodeId, long lastNodeTs) {
    }

    public void onCurrentNodeActivate() {
        init(maxSize);
    }

    public void onCurrentNodeLost() {
        this.active = false;
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

    public void notifyChange(int queueSize, int delta) {
        if (delta < 0 && waitingForTransfer > 0) {
            transferLock.lock();
            maxTransferCount.set(memoryCache.getMaxSize() - queueSize);
            try {
                retransmitCondition.signalAll();
            } finally {
                transferLock.unlock();
            }

        }
    }

    public void put(Collection<E> events) throws SinkException {
        if (!active) {
            throw new SinkException.Closed("Sink closed!");
        }

        if (!manager.getTransactionManager().hasTransaction()) {
            throw new SinkException("Use persistent queue only on transaction!");
        }

        try {
            putLock.lockInterruptibly();
        } catch (InterruptedException e) {
            throw new SinkException.Closed(e);
        }
        try {
            if (waitingForTransfer > 0) {
                throw new SinkException.Clogged("Retransmit events from failed nodes. Queue is clogged!");
            }

            ArrayList<EventWithKey> result = new ArrayList<EventWithKey>(events.size());
            for (E e : events) {
                result.add(new EventWithKey(e));
            }
            memoryCache.put(result);
            persistenceController.persist(result, ClusterService.getNodeId());
        } finally {
            putLock.unlock();
        }
    }

    public Collection<E> tryPut(Collection<E> events) throws SinkException {
        if (!active) {
            throw new SinkException.Closed("Sink closed!");
        }

        if (!manager.getTransactionManager().hasTransaction()) {
            throw new SinkException("Use persistent queue only on transaction!");
        }

        try {
            putLock.lockInterruptibly();
        } catch (InterruptedException e) {
            throw new SinkException.Closed(e);
        }
        try {
            if (waitingForTransfer > 0) {
                return new ArrayList<E>(events);
            }

            ArrayList<EventWithKey> result = new ArrayList<EventWithKey>(events.size());
            for (E e : events) {
                result.add(new EventWithKey(e));
            }
            Collection<EventWithKey> decline = memoryCache.tryPut(result);
            if (decline != null) {
                result.removeAll(decline);
            }
            persistenceController.persist(result, ClusterService.getNodeId());
            return EventWithKey.extract(decline);
        } finally {
            putLock.unlock();
        }
    }

    public Collection<E> take(int count) throws SourceException {
        if (!active) {
            return null;
        }

        if (!manager.getTransactionManager().hasTransaction()) {
            throw new SourceException("Use persistent queue only on transaction!");
        }
        try {
            takeLock.lockInterruptibly();
        } catch (InterruptedException e) {
            throw new SourceException(e);
        }
        try {
            Collection<EventWithKey> result = memoryCache.take(count);
            if (result == null) {
                return null;
            }
            persistenceController.delete(result, ClusterService.getNodeId());

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

    protected AbstractTransactionalMemoryQueue<EventWithKey> createCache(IJedaManager manager, int maxSize) {
        return new TransactionalMemoryQueue<EventWithKey>(manager, ObjectCloneType.SERIALIZE, maxSize);
    }

    void waitForFreeSlots() throws SinkException.Closed {
        transferLock.lock();

        try {
            while (maxTransferCount.get() <= 0l) {
                try {
                    retransmitCondition.await(1000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    throw new SinkException.Closed(e);
                }
            }

        } finally {
            transferLock.unlock();
        }
    }

    boolean transfer(long nodeId) throws SinkException.Closed {
        if (!active) {
            throw new SinkException.Closed("Sink deactivated");
        }
        try {
            transferLock.lockInterruptibly();
        } catch (InterruptedException e) {
            throw new SinkException.Closed(e);
        }

        int count = maxTransferCount.get();
        try {
            if (count > 0) {
                Collection<EventWithKey> result = persistenceController.transfer(count, nodeId, ClusterService.getNodeId());
                if (result != null) {
                    memoryCache.put(result);
                    maxTransferCount.addAndGet(-result.size());
                    if (result.size() < count) {
                        waitingForTransfer--;
                        return true;
                    }
                } else {
                    return true;
                }
            }
            return false;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            transferLock.unlock();
        }
    }

    private void init(int maxSize) {
        memoryCache = createCache(manager, maxSize);
        memoryCache.setObserver(observer);
        ITransactionManagerInternal tm = manager.getTransactionManager();
        tm.begin();
        try {
            Collection<EventWithKey> load = persistenceController.load(ClusterService.getNodeId());
            if (load != null && !load.isEmpty()) {
                memoryCache.put(load);
                memoryCache.getCurrentTx().registerCallback(new ActivateOnPush());
            } else {
                active = true;
            }

            tm.commit();
        } catch (Throwable e) {
            tm.rollback();
            throw new RuntimeException(e);
        }
    }

    private final class ActivateOnPush implements Tx.Callback {
        public void beforeCompletion(boolean success) {
            if (success) {
                active = true;
            }
        }

        public void afterCompletion(boolean success) {
        }
    }
}
