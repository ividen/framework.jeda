package ru.kwanza.jeda.persistentqueue.jdbc;

import ru.kwanza.dbtool.orm.annotations.Field;
import ru.kwanza.dbtool.orm.annotations.IdField;
import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.jeda.persistentqueue.DefaultPersistableEvent;
import ru.kwanza.jeda.persistentqueue.IPersistableEvent;
import ru.kwanza.toolbox.SerializationHelper;

/**
 * @author Alexander Guzanov
 */
public class EventQueue<E extends DefaultPersistableEvent> implements IEventRecord {
    @IdField("id")
    private Long id;
    @Field("node_id")
    private Integer nodeId;
    @Field("data")
    private byte[] eventData;

    public EventQueue(Long id, Integer nodeId, byte[] eventData) {
        this.id = id;
        this.nodeId = nodeId;
        this.eventData = eventData;
    }

    public E getEvent() {
        E result = null;
        try {
            result = (E) SerializationHelper.bytesToObject(eventData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        result.setPersistId(id);
        return result;
    }

    public Long getId() {
        return id;
    }

    public Integer getNodeId() {
        return nodeId;
    }
}
