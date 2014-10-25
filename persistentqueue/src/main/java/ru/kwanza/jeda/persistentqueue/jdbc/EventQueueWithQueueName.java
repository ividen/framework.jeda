package ru.kwanza.jeda.persistentqueue.jdbc;

import ru.kwanza.dbtool.orm.annotations.Field;
import ru.kwanza.jeda.persistentqueue.DefaultPersistableEvent;

/**
 * @author Alexander Guzanov
 */
public class EventQueueWithQueueName<E extends DefaultPersistableEvent> extends EventQueue<E>{
    @Field("queue_name")
    private String queueName;

    public String getQueueName() {
        return queueName;
    }
}
