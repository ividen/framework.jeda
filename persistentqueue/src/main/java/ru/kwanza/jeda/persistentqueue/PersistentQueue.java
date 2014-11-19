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
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
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
    private Map<Node,AbstractTransactionalMemoryQueue<E>> repairableNodes;
    private volatile boolean active = false;
    private int maxSize;
    private AtomicInteger size = new AtomicInteger(0);

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
            memoryCache = createCache();
            ITransactionManagerInternal tm = manager.getTransactionManager();
            tm.begin();
            try {

                Collection<E> load = persistenceController.load(maxSize, clusterService.getCurrentNode());
                if (load != null && !load.isEmpty()) {
                    memoryCache.put(load);
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

    public void handleStartRepair(Node node) {
        final AbstractTransactionalMemoryQueue<E> nodeCache = createCache();
        repairableNodes.put(node,nodeCache);
        final Collection<E> elements = persistenceController.load(maxSize, node);
        if(elements!=null && !elements.isEmpty()) {
            try {
                nodeCache.put(elements);
            } catch (SinkException e) {
            }

        }else{
            clusterService.markRepaired(this,node);
        }
    }

    public void handleStopRepair(Node node) {
        final AbstractTransactionalMemoryQueue<E> remove = repairableNodes.remove(node);
        if(remove!=null){

        }

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
        size.addAndGet(delta);
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

            Collection<E> result = memoryCache.take(count);

            persistenceController.delete(result, clusterService.getCurrentNode());

            return result;
        } finally {
            takeLock.unlock();
        }
    }

    public int size() {
        return active ? size.get() : 0;
    }

    public boolean isActive() {
        return active;
    }

    private AbstractTransactionalMemoryQueue<E> createCache() {
        final AbstractTransactionalMemoryQueue<E> result = createMemoryQueue(manager, maxSize);
        result.setObserver(observer);

        return result;
    }

    protected AbstractTransactionalMemoryQueue<E> createMemoryQueue(IJedaManager manager, int maxSize){
        return new TransactionalMemoryQueue<E>(manager, ObjectCloneType.SERIALIZE, maxSize);
    }



}
