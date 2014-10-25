package ru.kwanza.jeda.persistentqueue.jdbc;

import ru.kwanza.dbtool.orm.annotations.Field;
import ru.kwanza.jeda.persistentqueue.DefaultPriorityPersistableEvent;

/**
 * @author Alexander Guzanov
 */
public class PriorityEventQueueWithQueueName<E extends DefaultPriorityPersistableEvent> extends PriorityEventQueue<E> {
    @Field("queue_name")
    private String queueName;

    public String getQueueName() {
        return queueName;
    }
}
