package ru.kwanza.jeda.persistentqueue;

import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.clusterservice.IClusterService;
import ru.kwanza.jeda.core.queue.AbstractTransactionalMemoryQueue;
import ru.kwanza.jeda.core.queue.ObjectCloneType;
import ru.kwanza.jeda.core.queue.PriorityTransactionalMemoryQueue;

/**
 * @author Guzanov Alexander
 */
public class PriorityPersistentQueue<E extends IPriorityPersistableEvent> extends PersistentQueue<E> {

    public PriorityPersistentQueue(IJedaManager manager, IClusterService clusterService,
                                   int maxSize, IQueuePersistenceController<E> controller) {
        super(manager, clusterService, maxSize, controller);
    }

    @Override
    protected AbstractTransactionalMemoryQueue<E> createMemoryQueue(IJedaManager manager, int maxSize) {
        return new PriorityTransactionalMemoryQueue<E>(manager, ObjectCloneType.SERIALIZE, maxSize);
    }
}
