package ru.kwanza.jeda.persistentqueue.jdbc.base;

import ru.kwanza.dbtool.orm.annotations.Field;
import ru.kwanza.dbtool.orm.annotations.IdField;
import ru.kwanza.jeda.persistentqueue.IPersistableEvent;
import ru.kwanza.jeda.persistentqueue.jdbc.IEventRecord;
import ru.kwanza.toolbox.SerializationHelper;

/**
 * @author Alexander Guzanov
 */
public class BaseEventQueue<E extends IPersistableEvent> implements IEventRecord {
    @IdField("id")
    protected Long id;
    @Field("node_id")
    protected Integer nodeId;
    @Field("data")
    protected byte[] eventData;

    public BaseEventQueue(Long id, Integer nodeId, byte[] eventData) {
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

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }
}
