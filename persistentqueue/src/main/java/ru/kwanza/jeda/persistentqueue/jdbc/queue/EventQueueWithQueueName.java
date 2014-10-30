package ru.kwanza.jeda.persistentqueue.jdbc.queue;

import ru.kwanza.dbtool.orm.annotations.Entity;
import ru.kwanza.dbtool.orm.annotations.Field;
import ru.kwanza.jeda.persistentqueue.DefaultPersistableEvent;
import ru.kwanza.jeda.persistentqueue.jdbc.IEventRecordBuilder;
import ru.kwanza.jeda.persistentqueue.jdbc.base.BaseEventQueueWithQueueName;
import ru.kwanza.toolbox.SerializationHelper;

/**
 * @author Alexander Guzanov
 */
@Entity(name = "jeda.persistentqueue.jdbc.EventQueueWithQueueName", table = "jeda_jdbc_event_nqueue")
public class EventQueueWithQueueName<E extends DefaultPersistableEvent> extends BaseEventQueueWithQueueName<E> {


    public EventQueueWithQueueName(Long id, Integer nodeId, byte[] eventData, String queueName) {
        super(id, nodeId, eventData, queueName);
    }

    public static class Builder implements IEventRecordBuilder<EventQueueWithQueueName, DefaultPersistableEvent> {
        private String queueName;

        public Builder(String queueName) {
            this.queueName = queueName;
        }

        public EventQueueWithQueueName build(DefaultPersistableEvent event, int nodeId) {
            try {
                return new EventQueueWithQueueName(event.getPersistId(), nodeId, SerializationHelper.objectToBytes(event), queueName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
