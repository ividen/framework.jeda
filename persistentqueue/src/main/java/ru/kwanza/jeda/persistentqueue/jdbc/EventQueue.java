package ru.kwanza.jeda.persistentqueue.jdbc;

import liquibase.util.StreamUtil;
import ru.kwanza.dbtool.orm.annotations.Entity;
import ru.kwanza.dbtool.orm.annotations.Field;
import ru.kwanza.dbtool.orm.annotations.IdField;
import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.persistentqueue.DefaultPersistableEvent;
import ru.kwanza.toolbox.SerializationHelper;

/**
 * @author Alexander Guzanov
 */
public class EventQueue<E extends DefaultPersistableEvent> {
    @IdField("id")
    private Long id;
    @Field("node_id")
    private Integer nodeId;
    @Field("data")
    private byte[] eventData;


    public E getEvent() throws Exception {
        E result = (E) SerializationHelper.bytesToObject(eventData);
        result.setPersistId(id);
        return result;
    }
}
