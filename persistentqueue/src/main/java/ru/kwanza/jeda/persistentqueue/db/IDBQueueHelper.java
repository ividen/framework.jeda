package ru.kwanza.jeda.persistentqueue.db;

import ru.kwanza.jeda.persistentqueue.IPersistableEvent;
import ru.kwanza.toolbox.fieldhelper.FieldHelper;

/**
 * @author Alexander Guzanov
 */
public interface IDBQueueHelper<R extends IEventRecord, E extends IPersistableEvent> {

    Class<R> getORMClass();

    R buildRecord(E event, int nodeId);

    FieldHelper.Field<R,E> getEvent();

    String getQueueNameField();

    String getQueueNameValue();

    String getIdField();

    String getNodeIdField();

}
