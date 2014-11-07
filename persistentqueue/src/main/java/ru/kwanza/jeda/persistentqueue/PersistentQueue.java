package ru.kwanza.jeda.persistentqueue;

import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.api.internal.IQueueObserver;
import ru.kwanza.jeda.api.internal.ITransactionManagerInternal;
import ru.kwanza.jeda.api.internal.SourceException;
import ru.kwanza.jeda.clusterservice.IClusterService;
import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.jeda.core.queue.AbstractTransactionalMemoryQueue;
import ru.kwanza.jeda.core.queue.ObjectCloneType;
import ru.kwanza.jeda.core.queue.QueueObserverChain;
import ru.kwanza.jeda.core.queue.TransactionalMemoryQueue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Guzanov Alexander
 */
public class PersistentQueue<E extends IPersistableEvent> implements IQueue<E>, IClusteredComponent, IQueueObserver {
    private AbstractTransactionalMemoryQueue<E> memoryCache;
    private IQueuePersistenceController<E> persistenceController;
    private IQueueObserver originalObserver;
    private IClusterService clusterService;
    private IJedaManager manager;
    private QueueObserverChain observer;
    private ReentrantLock putLock = new ReentrantLock();
    private ReentrantLock takeLock = new ReentrantLock();
    private volatile boolean active = false;
    private volatile boolean tryLoad = false;
    private int maxSize;
    private int repairIterationItemCount = 100;

    public PersistentQueue(IJedaManager manager, IClusterService clusterService,
                           int maxSize, IQueuePersistenceController<E> controller) {
        observer = new QueueObserverChain();
        observer.addObserver(this);
        this.manager = manager;
        this.persistenceController = controller;
        this.clusterService = clusterService;
        this.maxSize = maxSize;

        clusterService.registerComponent(this);
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

                Collection<E> load = persistenceController.load(maxSize, clusterService.getCurrentNode());
                if (load != null && !load.isEmpty()) {
                    memoryCache.put(load);
                    if (load.size() >= maxSize) {
                        tryLoad = true;
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

    public void setRepairIterationItemCount(int repairIterationItemCount) {
        this.repairIterationItemCount = repairIterationItemCount;
    }

    public int getRepairIterationItemCount() {
        return repairIterationItemCount;
    }


    public boolean handleStartRepair(Node reparableNode) {
        int memorySize = memoryCache.size();
        int count = 0;
        try {
            count = persistenceController.transfer(getRepairIterationItemCount(), clusterService.getCurrentNode(),
                    reparableNode);
            takeLock.lock();
            try {
                tryLoad = true;
            } finally {
                takeLock.unlock();
            }
            return count < getRepairIterationItemCount();
        } finally {
            getObserver().notifyChange(memorySize + count, count);
        }
    }

    public void handleStopRepair(Node node) {

    }

    public String getName() {
        return "jeda.PersistentQueue." + persistenceController.getQueueName();
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

            memoryCache.put(events);
            persistenceController.persist(events, clusterService.getCurrentNode());
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

            ArrayList<E> copy = new ArrayList<E>(events);
            Collection<E> decline = memoryCache.tryPut(copy);
            if (decline != null) {
                copy.removeAll(decline);
            }
            persistenceController.persist(copy, clusterService.getCurrentNode());
            return decline;
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

            Collection<E> result;
            if (tryLoad) {
                result = new ArrayList<E>(count);
                Collection<E> take = memoryCache.take(Math.max(count / 2, 1));
                int c = 0;
                if (take != null && !take.isEmpty()) {
                    result.addAll(take);
                    c = take.size();
                }
                Collection<E> load = persistenceController.load(count - c, clusterService.getCurrentNode());
                if (load == null || load.isEmpty()) {
                    tryLoad = false;
                } else {
                    result.addAll(load);
                }

            } else {
                result = memoryCache.take(count);
            }

            persistenceController.delete(result, clusterService.getCurrentNode());

            return result;
        } finally {
            takeLock.unlock();
        }
    }

    public int size() {
        return active ? memoryCache.size() : 0;
    }

    public boolean isActive() {
        return active;
    }

    protected AbstractTransactionalMemoryQueue<E> createCache(IJedaManager manager, int maxSize) {
        return new TransactionalMemoryQueue<E>(manager, ObjectCloneType.SERIALIZE, maxSize);
    }


}
