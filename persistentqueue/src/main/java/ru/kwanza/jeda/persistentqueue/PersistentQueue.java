package ru.kwanza.jeda.persistentqueue;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.api.internal.IQueueObserver;
import ru.kwanza.jeda.api.internal.ITransactionManagerInternal;
import ru.kwanza.jeda.api.internal.SourceException;
import ru.kwanza.jeda.clusterservice.IClusterService;
import ru.kwanza.jeda.clusterservice.IClusteredModule;
import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.jeda.core.queue.AbstractTransactionalMemoryQueue;
import ru.kwanza.jeda.core.queue.ObjectCloneType;
import ru.kwanza.jeda.core.queue.QueueObserverChain;
import ru.kwanza.jeda.core.queue.TransactionalMemoryQueue;
import ru.kwanza.jeda.persistentqueue.old.EventWithKey;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Guzanov Alexander
 */
public class PersistentQueue<E extends IEvent> implements IQueue<E>, IClusteredModule, IQueueObserver {
    private AbstractTransactionalMemoryQueue<EventWithKey> memoryCache;
    private IQueuePersistenceController persistenceController;
    private IQueueObserver originalObserver;
    private IClusterService clusterService;
    private IJedaManager manager;
    private QueueObserverChain observer;
    private ReentrantLock putLock = new ReentrantLock();
    private ReentrantLock takeLock = new ReentrantLock();
    private volatile boolean active = false;
    private int maxSize;
    private int repairIterationItemCount = 100;
    private AtomicInteger expectedRepairing = new AtomicInteger(0);

    public PersistentQueue(IJedaManager manager, IClusterService clusterService,
                           int maxSize, IQueuePersistenceController controller) {
        observer = new QueueObserverChain();
        observer.addObserver(this);
        this.manager = manager;
        this.persistenceController = controller;
        this.clusterService = clusterService;
        this.maxSize = maxSize;

        clusterService.registerModule(this);
    }

    public IJedaManager getManager() {
        return manager;
    }

    public void handleStart() {
        takeLock.lock();
        putLock.lock();
        try {
            memoryCache = createCache(manager, maxSize);
            memoryCache.setObserver(observer);
            ITransactionManagerInternal tm = manager.getTransactionManager();
            tm.begin();
            try {
                int totalCount = persistenceController.getTotalCount(clusterService.getCurrentNode());
                if (totalCount > maxSize) {
                    expectedRepairing.set(totalCount - maxSize);
                }
                if (totalCount > 0) {
                    Collection<EventWithKey> load = persistenceController.load(maxSize, clusterService.getCurrentNode());
                    if (load != null && !load.isEmpty()) {
                        memoryCache.put(load);
                    }
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

    public void handleStop() {
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

    public int getMaxSize() {
        return maxSize;
    }

    public int getRepairIterationItemCount() {
        return repairIterationItemCount;
    }

    public int getExpectedRepairing() {
        return expectedRepairing.get();
    }

    public boolean handleRepair(Node reparableNode) {
        int count = persistenceController.transfer(getRepairIterationItemCount(), clusterService.getCurrentNode(),
                reparableNode);
        expectedRepairing.addAndGet(count);
        return count < getRepairIterationItemCount();
    }

    public String getName() {
        return "jeda.QueuePersistenceController." + persistenceController.getQueueName();
    }

    private Collection<EventWithKey> repair(final int count) {
        Collection<EventWithKey> result = persistenceController.load(count, clusterService.getCurrentNode());
        try {
            manager.getTransactionManager().getTransaction().registerSynchronization(new ExpectedRepairingSync(result.size()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }


    private class ExpectedRepairingSync implements Synchronization {
        private final int size;

        public ExpectedRepairingSync(int size) {
            this.size = size;
        }

        public void beforeCompletion() {

        }

        public void afterCompletion(int i) {
            if (i == Status.STATUS_COMMITTED) {
                expectedRepairing.addAndGet(size);
            }
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
        return active ? (memoryCache.getEstimatedCount() + getExpectedRepairing()) : 0;
    }

    public boolean isReady() {
        return memoryCache.isReady();
    }

    public void notifyChange(int queueSize, int delta) {
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
            persistenceController.persist(result, clusterService.getCurrentNode());
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
            persistenceController.persist(result, clusterService.getCurrentNode());
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

            Collection<EventWithKey> result;
            if (getExpectedRepairing() > 0) {
                result = repair(count);
                Collection<EventWithKey> take = memoryCache.take(result.size() - count);
                if (take != null) {
                    result.addAll(take);
                }
            } else {
                result = memoryCache.take(count);
            }
            if (result == null) {
                return null;
            }
            persistenceController.delete(result, clusterService.getCurrentNode());

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


}
