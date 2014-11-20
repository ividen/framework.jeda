package ru.kwanza.jeda.persistentqueue.db;

import ru.kwanza.jeda.persistentqueue.IPersistableEvent;

/**
 * @author Alexander Guzanov
 */
public interface IDBQueueHelper<R extends IEventRecord, E extends IPersistableEvent> {

    Class<R> getORMClass();

    R buildRecord(E event, int nodeId);

    String getQueueNameField();

    String getQueueNameValue();

    String getIdField();

    String getNodeIdField();
}
