package ru.kwanza.jeda.persistentqueue;

import ru.kwanza.jeda.api.IPriorityEvent;
import ru.kwanza.jeda.api.internal.ISystemManager;
import ru.kwanza.jeda.core.queue.AbstractTransactionalMemoryQueue;
import ru.kwanza.jeda.core.queue.ObjectCloneType;
import ru.kwanza.jeda.core.queue.PriorityTransactionalMemoryQueue;

/**
 * @author Guzanov Alexander
 */
public class PriorityPersistentQueue<E extends IPriorityEvent> extends PersistentQueue<E> {
    public PriorityPersistentQueue(ISystemManager manager, long maxSize, IQueuePersistenceController controller) {
        super(manager, maxSize, controller);
    }

    @Override
    protected AbstractTransactionalMemoryQueue<EventWithKey> createCache(ISystemManager manager, long maxSize) {
        return new PriorityTransactionalMemoryQueue<EventWithKey>(manager, ObjectCloneType.SERIALIZE, maxSize);
    }
}
