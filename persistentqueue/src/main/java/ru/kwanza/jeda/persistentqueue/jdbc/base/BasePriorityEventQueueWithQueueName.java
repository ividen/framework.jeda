package ru.kwanza.jeda.persistentqueue.jdbc.base;

import ru.kwanza.dbtool.orm.annotations.Entity;
import ru.kwanza.dbtool.orm.annotations.Field;
import ru.kwanza.jeda.persistentqueue.DefaultPriorityPersistableEvent;

/**
 * @author Alexander Guzanov
 */
public class BasePriorityEventQueueWithQueueName<E extends DefaultPriorityPersistableEvent> extends BasePriorityEventQueue<E> {
    @Field("queue_name")
    private String queueName;

    public BasePriorityEventQueueWithQueueName(Long id, Integer nodeId, byte[] eventData, Integer priority, String queueName) {
        super(id, nodeId, eventData, priority);
        this.queueName = queueName;
    }

    public String getQueueName() {
        return queueName;
    }
}
