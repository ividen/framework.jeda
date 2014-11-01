package ru.kwanza.jeda.persistentqueue.db;

import ru.kwanza.jeda.persistentqueue.IPersistableEvent;

/**
 * @author Alexander Guzanov
 */
public interface IEventRecord<E extends IPersistableEvent>{
    int getNodeId();

    void setNodeId(int nodeId);

    E getEvent();
}
