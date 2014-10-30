package ru.kwanza.jeda.persistentqueue.jdbc.queue;

import ru.kwanza.dbtool.orm.annotations.Entity;
import ru.kwanza.dbtool.orm.annotations.Field;
import ru.kwanza.jeda.api.IPriorityEvent;
import ru.kwanza.jeda.persistentqueue.DefaultPriorityPersistableEvent;
import ru.kwanza.jeda.persistentqueue.jdbc.IEventRecordBuilder;
import ru.kwanza.jeda.persistentqueue.jdbc.base.BasePriorityEventQueue;
import ru.kwanza.toolbox.SerializationHelper;

/**
 * @author Alexander Guzanov
 */
@Entity(name = "jeda.persistentqueue.jdbc.PriorityEventQueue", table = "jeda_jdbc_event_pqueue")
public class PriorityEventQueue<E extends DefaultPriorityPersistableEvent> extends BasePriorityEventQueue<E> {
    public PriorityEventQueue(Long id, Integer nodeId, byte[] eventData, Integer priority) {
        super(id, nodeId, eventData, priority);
    }

    public static class Builder implements IEventRecordBuilder<PriorityEventQueue, DefaultPriorityPersistableEvent> {
        public PriorityEventQueue build(DefaultPriorityPersistableEvent event, int nodeId) {
            try {
                return new PriorityEventQueue(event.getPersistId(), nodeId,
                        SerializationHelper.objectToBytes(event), event.getPriority().getCode());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
