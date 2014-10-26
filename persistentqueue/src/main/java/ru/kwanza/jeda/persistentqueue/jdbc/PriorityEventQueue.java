package ru.kwanza.jeda.persistentqueue.jdbc;

import ru.kwanza.dbtool.orm.annotations.Field;
import ru.kwanza.dbtool.orm.annotations.IdField;
import ru.kwanza.jeda.api.IPriorityEvent;
import ru.kwanza.jeda.persistentqueue.DefaultPersistableEvent;
import ru.kwanza.jeda.persistentqueue.DefaultPriorityPersistableEvent;
import ru.kwanza.toolbox.SerializationHelper;

/**
 * @author Alexander Guzanov
 */
public class PriorityEventQueue<E extends DefaultPriorityPersistableEvent> extends EventQueue<E>{
    @Field("priority")
    private Integer priority;

    public PriorityEventQueue(Long id, Integer nodeId, byte[] eventData) {
        super(id, nodeId, eventData);
    }

    public E getEvent()  {
        E event = super.getEvent();
        event.setPriority(IPriorityEvent.Priority.findByCode(priority));
        return event;
    }
}
