package ru.kwanza.jeda.persistentqueue.jdbc.queue;

import ru.kwanza.dbtool.orm.annotations.Entity;
import ru.kwanza.dbtool.orm.api.If;
import ru.kwanza.jeda.persistentqueue.DefaultPersistableEvent;
import ru.kwanza.jeda.persistentqueue.jdbc.IEventRecordBuilder;
import ru.kwanza.jeda.persistentqueue.jdbc.base.BaseEventQueue;
import ru.kwanza.toolbox.SerializationHelper;

/**
 * @author Alexander Guzanov
 */
@Entity(name = "jeda.persistentqueue.jdbc.EventQueue", table = "jeda_jdbc_event_queue")
public class EventQueue<E extends DefaultPersistableEvent> extends BaseEventQueue<E> {

    public EventQueue(Long id, Integer nodeId, byte[] eventData) {
        super(id, nodeId, eventData);
    }

    public static class Builder implements IEventRecordBuilder<BaseEventQueue, DefaultPersistableEvent> {
        public EventQueue build(DefaultPersistableEvent event, int nodeId) {
            try {
                return new EventQueue(event.getPersistId(), nodeId, SerializationHelper.objectToBytes(event));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public If condition() {
            return null;
        }

        public String getConditionAsString() {
            return null;
        }
    }

}
