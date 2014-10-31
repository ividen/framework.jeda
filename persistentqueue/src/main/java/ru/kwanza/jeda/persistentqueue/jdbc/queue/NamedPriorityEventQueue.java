package ru.kwanza.jeda.persistentqueue.jdbc.queue;

import ru.kwanza.dbtool.orm.annotations.Entity;
import ru.kwanza.dbtool.orm.api.If;
import ru.kwanza.jeda.persistentqueue.DefaultPriorityPersistableEvent;
import ru.kwanza.jeda.persistentqueue.jdbc.IEventRecordBuilder;
import ru.kwanza.jeda.persistentqueue.jdbc.base.BasePriorityEventQueueWithQueueName;
import ru.kwanza.toolbox.SerializationHelper;

/**
 * @author Alexander Guzanov
 */
@Entity(name = "jeda.persistentqueue.jdbc.NamedPriorityEventQueue", table = "jeda_jdbc_event_pnqueue")
public class NamedPriorityEventQueue<E extends DefaultPriorityPersistableEvent> extends BasePriorityEventQueueWithQueueName<E> {
    public NamedPriorityEventQueue(Long id, Integer nodeId, byte[] eventData, Integer priority, String queueName) {
        super(id, nodeId, eventData, priority, queueName);
    }

    public static class Builder implements IEventRecordBuilder<NamedPriorityEventQueue, DefaultPriorityPersistableEvent> {
        private String queueName;

        public Builder(String queueName) {
            this.queueName = queueName;
        }

        public NamedPriorityEventQueue build(DefaultPriorityPersistableEvent event, int nodeId) {
            try {
                return new NamedPriorityEventQueue(event.getPersistId(), nodeId,
                        SerializationHelper.objectToBytes(event), event.getPriority().getCode(), queueName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public If condition() {
            return If.isEqual("queueName",If.valueOf(queueName));
        }

        public String getConditionAsString() {
            return "queueName="+queueName;
        }
    }
}
