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
import ru.kwanza.jeda.clusterservice.impl.db.ComponentInActiveExcetion;
import ru.kwanza.jeda.core.queue.AbstractTransactionalMemoryQueue;
import ru.kwanza.jeda.core.queue.ObjectCloneType;
import ru.kwanza.jeda.core.queue.QueueObserverChain;
import ru.kwanza.jeda.core.queue.TransactionalMemoryQueue;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Guzanov Alexander
 */
public class PersistentQueue<E extends IPersistableEvent> implements IQueue<E>, IClusteredComponent, IQueueObserver {
    private AbstractTransactionalMemoryQueue<E> memoryCache;
    private IQueuePersistenceController<E> persistenceController;
    private IClusterService clusterService;
    private IJedaManager manager;
    private QueueObserverChain observer;
    private ConcurrentMap<Node, AbstractTransactionalMemoryQueue<E>> repairableNodes
            = new ConcurrentHashMap<Node, AbstractTransactionalMemoryQueue<E>>();
    private volatile boolean active = false;
    private int maxSize;
    private AtomicInteger size = new AtomicInteger(0);

    public PersistentQueue(IJedaManager manager, IClusterService clusterService,
                           int maxSize, IQueuePersistenceController<E> controller) {
        this.observer = new QueueObserverChain();
        this.observer.addObserver(this);
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
    }

    public void handleStop() {
        active = false;
        repairableNodes.clear();
        size.set(0);
//        persistenceController.closePersistentStore(clusterService.getCurrentNode());
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void handleStartRepair(Node node) {
        final AbstractTransactionalMemoryQueue<E> queue = createCache();

        manager.getTransactionManager().begin();

        try {
            final Collection<E> elements = persistenceController.load(maxSize, node);
            if (elements != null && !elements.isEmpty()) {
                try {
                    queue.put(elements);
                } catch (SinkException e) {
                }

                repairableNodes.put(node, queue);

            } else {
                clusterService.markRepaired(this, node);
            }
        }finally {
            manager.getTransactionManager().commit();
        }
    }

    public void handleStopRepair(Node node) {
        removeQueue(node);
        persistenceController.closePersistentStore(node);
    }

    private void removeQueue(Node node) {
        final AbstractTransactionalMemoryQueue<E> remove;
        remove = repairableNodes.remove(node);
        if (remove != null) {
            notifyChange(size() - remove.size(), -remove.size());
        }
    }

    public String getName() {
        return "jeda.PersistentQueue." + persistenceController.getQueueName();
    }

    public void setObserver(IQueueObserver observer) {
        this.observer.addObserver(observer);
    }

    public IQueueObserver getObserver() {
        return observer;
    }

    public int getEstimatedCount() {
        return active ? size() : 0;
    }

    public boolean isReady() {
        return memoryCache.isReady();
    }

    public void notifyChange(int queueSize, int delta) {
        size.addAndGet(delta);
    }

    public void put(final Collection<E> events) throws SinkException {
        if (!manager.getTransactionManager().hasTransaction()) {
            throw new SinkException("Use persistent queue only on transaction!");
        }

        if (!active) {
            throw new SinkException.Closed("Sink closed!");
        }

        try {
            clusterService.criticalSection(this, new Callable<Object>() {
                public Object call() throws Exception {
                    doPut(events);
                    return null;
                }
            });
        } catch (InvocationTargetException e) {
            throw new SinkException(e.getCause());
        } catch (ComponentInActiveExcetion e) {
            throw new SinkException.Closed(e);
        }

    }


    public Collection<E> tryPut(final Collection<E> events) throws SinkException {
        if (!manager.getTransactionManager().hasTransaction()) {
            throw new SinkException("Use persistent queue only on transaction!");
        }

        if (!active) {
            throw new SinkException.Closed("Sink closed!");
        }

        try {
            return clusterService.criticalSection(this, new Callable<Collection<E>>() {
                public Collection<E> call() throws Exception {
                    return doTryPut(events);
                }
            });
        } catch (InvocationTargetException e) {
            throw new SinkException(e.getCause());
        } catch (ComponentInActiveExcetion e) {
            throw new SinkException.Closed(e);
        }
    }


    public Collection<E> take(final int count) throws SourceException {
        if (!manager.getTransactionManager().hasTransaction()) {
            throw new SourceException("Use persistent queue only on transaction!");
        }

        if (!active) {
            return null;
        }

        try {
            return clusterService.criticalSection(this, new Callable<Collection<E>>() {
                public Collection<E> call() throws Exception {
                    return doTake(count);
                }
            });
        } catch (InvocationTargetException e) {
            throw new SourceException(e.getCause());
        } catch (ComponentInActiveExcetion e) {
            throw new SourceException(e);
        }
    }

    private Collection<E> doTryPut(Collection<E> events) throws SinkException {
        ArrayList<E> copy = new ArrayList<E>(events);
        Collection<E> decline = memoryCache.tryPut(copy);
        if (decline != null) {
            copy.removeAll(decline);
        }
        persistenceController.persist(copy, clusterService.getCurrentNode());
        return decline;
    }

    private void doPut(Collection<E> events) throws SinkException {
        memoryCache.put(events);
        persistenceController.persist(events, clusterService.getCurrentNode());
    }


    private Collection<E> doTake(int count) throws SourceException {
        Collection<E> result;

        if (!repairableNodes.isEmpty()) {
            result = repairTake(count);
        } else {
            result = memoryCache.take(count);
            if (result != null && !result.isEmpty()) {
                persistenceController.delete(result, clusterService.getCurrentNode());
            }
        }

        return result;
    }

    private Collection<E> repairTake(int count) throws SourceException {
        Collection<E> result;
        int i = repairableNodes.size() + 1;
        int takeCount = Math.max(1, count / i);
        int restCount = count;
        result = new ArrayList<E>(count);
        Collection<E> take;
        Iterator<Map.Entry<Node, AbstractTransactionalMemoryQueue<E>>> iterator = new HashMap(repairableNodes).entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Node, AbstractTransactionalMemoryQueue<E>> e = iterator.next();
            AbstractTransactionalMemoryQueue<E> queue = e.getValue();
            Node node = e.getKey();
            take = queue.take(takeCount);
            i--;
            if (take != null && !take.isEmpty()) {
                result.addAll(take);
                persistenceController.delete(take, node);
                restCount -= take.size();
            } else {
                removeQueue(node);
                try {
                    manager.getTransactionManager().getTransaction().registerSynchronization(new MarkRepairedSynchronization(node));
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }

            if (restCount <= 0 || i <= 0) break;
            takeCount = Math.max(1, restCount / i);

        }
        if (restCount > 0) {
            take = memoryCache.take(restCount);
            if (take != null && !take.isEmpty()) {
                result.addAll(take);
                persistenceController.delete(take, clusterService.getCurrentNode());
            }
        }
        return result;
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

    protected AbstractTransactionalMemoryQueue<E> createMemoryQueue(IJedaManager manager, int maxSize) {
        return new TransactionalMemoryQueue<E>(manager, ObjectCloneType.SERIALIZE, maxSize);
    }


    private class MarkRepairedSynchronization implements Synchronization {
        private final Node node;

        public MarkRepairedSynchronization(Node node) {
            this.node = node;
        }

        public void beforeCompletion() {

        }

        public void afterCompletion(int status) {
            if (status == Status.STATUS_COMMITTED) {
                clusterService.markRepaired(PersistentQueue.this, node);
            }
        }
    }
}
