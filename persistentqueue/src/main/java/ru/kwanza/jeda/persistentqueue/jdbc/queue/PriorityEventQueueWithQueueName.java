package ru.kwanza.jeda.persistentqueue.jdbc.queue;

import ru.kwanza.dbtool.orm.annotations.Entity;
import ru.kwanza.jeda.persistentqueue.DefaultPriorityPersistableEvent;
import ru.kwanza.jeda.persistentqueue.jdbc.IEventRecordBuilder;
import ru.kwanza.jeda.persistentqueue.jdbc.base.BasePriorityEventQueueWithQueueName;
import ru.kwanza.toolbox.SerializationHelper;

/**
 * @author Alexander Guzanov
 */
@Entity(name = "jeda.persistentqueue.jdbc.PriorityEventQueueWithQueueName", table = "jeda_jdbc_event_pnqueue")
public class PriorityEventQueueWithQueueName<E extends DefaultPriorityPersistableEvent> extends BasePriorityEventQueueWithQueueName<E> {
    public PriorityEventQueueWithQueueName(Long id, Integer nodeId, byte[] eventData, Integer priority, String queueName) {
        super(id, nodeId, eventData, priority, queueName);
    }

    public static class Builder implements IEventRecordBuilder<PriorityEventQueueWithQueueName, DefaultPriorityPersistableEvent> {
        private String queueName;

        public Builder(String queueName) {
            this.queueName = queueName;
        }

        public PriorityEventQueueWithQueueName build(DefaultPriorityPersistableEvent event, int nodeId) {
            try {
                return new PriorityEventQueueWithQueueName(event.getPersistId(), nodeId,
                        SerializationHelper.objectToBytes(event), event.getPriority().getCode(), queueName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
