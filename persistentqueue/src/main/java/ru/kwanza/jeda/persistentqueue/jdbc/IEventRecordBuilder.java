package ru.kwanza.jeda.persistentqueue.jdbc;

import ru.kwanza.dbtool.orm.api.If;
import ru.kwanza.jeda.persistentqueue.IPersistableEvent;

/**
 * @author Alexander Guzanov
 */
public interface IEventRecordBuilder<R extends IEventRecord, E extends IPersistableEvent> {
    R build(E event, int nodeId);

    If condition();

    String getConditionAsString();
}
