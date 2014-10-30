package ru.kwanza.jeda.persistentqueue.jdbc.base;

import ru.kwanza.dbtool.orm.annotations.Entity;
import ru.kwanza.dbtool.orm.annotations.Field;
import ru.kwanza.jeda.persistentqueue.DefaultPersistableEvent;

/**
 * @author Alexander Guzanov
 */
public class BaseEventQueueWithQueueName<E extends DefaultPersistableEvent> extends BaseEventQueue<E> {
    @Field("queue_name")
    private String queueName;

    public BaseEventQueueWithQueueName(Long id, Integer nodeId, byte[] eventData, String queueName) {
        super(id, nodeId, eventData);
        this.queueName = queueName;
    }

    public String getQueueName() {
        return queueName;
    }
}
