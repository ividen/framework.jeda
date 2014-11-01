package ru.kwanza.jeda.persistentqueue.db;

import ru.kwanza.dbtool.orm.api.If;
import ru.kwanza.jeda.persistentqueue.IPersistableEvent;

/**
 * @author Alexander Guzanov
 */
public interface IEventRecordHelper<R extends IEventRecord, E extends IPersistableEvent> {

    Class<R> getORMClass();

    R buildRecord(E event, int nodeId);

    If getCondition();

    String getConditionAsString();
}
