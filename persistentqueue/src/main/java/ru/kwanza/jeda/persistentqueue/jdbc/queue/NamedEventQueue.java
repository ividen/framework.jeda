package ru.kwanza.jeda.persistentqueue.jdbc.queue;

import ru.kwanza.dbtool.orm.annotations.Entity;
import ru.kwanza.dbtool.orm.api.If;
import ru.kwanza.jeda.persistentqueue.DefaultPersistableEvent;
import ru.kwanza.jeda.persistentqueue.jdbc.IEventRecordBuilder;
import ru.kwanza.jeda.persistentqueue.jdbc.base.BaseNamedEventQueue;
import ru.kwanza.toolbox.SerializationHelper;

/**
 * @author Alexander Guzanov
 */
@Entity(name = "jeda.persistentqueue.jdbc.NamedEventQueue", table = "jeda_jdbc_event_nqueue")
public class NamedEventQueue<E extends DefaultPersistableEvent> extends BaseNamedEventQueue<E> {


    public NamedEventQueue(Long id, Integer nodeId, byte[] eventData, String queueName) {
        super(id, nodeId, eventData, queueName);
    }

    public static class Builder implements IEventRecordBuilder<NamedEventQueue, DefaultPersistableEvent> {
        private String queueName;

        public Builder(String queueName) {
            this.queueName = queueName;
        }

        public NamedEventQueue build(DefaultPersistableEvent event, int nodeId) {
            try {
                return new NamedEventQueue(event.getPersistId(), nodeId, SerializationHelper.objectToBytes(event), queueName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public If condition() {
            return If.isEqual("queueName", If.valueOf(queueName));
        }

        public String getConditionAsString() {
            return "queueName=" + queueName;
        }
    }
}
