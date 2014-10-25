package ru.kwanza.jeda.persistentqueue.old;

import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.IPriorityEvent;
import ru.kwanza.jeda.core.queue.AbstractTransactionalMemoryQueue;
import ru.kwanza.jeda.core.queue.ObjectCloneType;
import ru.kwanza.jeda.core.queue.PriorityTransactionalMemoryQueue;

/**
 * @author Guzanov Alexander
 */
public class PriorityPersistentQueue<E extends IPriorityEvent> extends PersistentQueue<E> {
    public PriorityPersistentQueue(IJedaManager manager, int maxSize, IQueuePersistenceController controller) {
        super(manager, maxSize, controller);
    }

    @Override
    protected AbstractTransactionalMemoryQueue<EventWithKey> createCache(IJedaManager manager, int maxSize) {
        return new PriorityTransactionalMemoryQueue<EventWithKey>(manager, ObjectCloneType.SERIALIZE, maxSize);
    }
}
