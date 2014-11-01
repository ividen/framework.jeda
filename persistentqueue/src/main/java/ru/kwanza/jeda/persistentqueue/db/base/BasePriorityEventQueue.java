package ru.kwanza.jeda.persistentqueue.db.base;

import ru.kwanza.dbtool.orm.annotations.Field;
import ru.kwanza.jeda.api.IPriorityEvent;
import ru.kwanza.jeda.persistentqueue.DefaultPriorityPersistableEvent;
import ru.kwanza.jeda.persistentqueue.db.queue.EventQueue;

/**
 * @author Alexander Guzanov
 */
public class BasePriorityEventQueue<E extends DefaultPriorityPersistableEvent> extends EventQueue<E> {
    @Field("priority")
    private Integer priority;

    public BasePriorityEventQueue(Long id, Integer nodeId, byte[] eventData, Integer priority) {
        super(id, nodeId, eventData);
        this.priority = priority;
    }

    public Integer getPriority() {
        return priority;
    }

    public E getEvent() {
        E event = super.getEvent();
        event.setPriority(IPriorityEvent.Priority.findByCode(priority));
        return event;
    }
}
