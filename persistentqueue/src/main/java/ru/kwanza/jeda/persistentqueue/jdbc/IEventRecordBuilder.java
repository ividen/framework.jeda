package ru.kwanza.jeda.persistentqueue.jdbc;

import ru.kwanza.jeda.persistentqueue.IPersistableEvent;

/**
 * @author Alexander Guzanov
 */
public interface IEventRecordBuilder<R extends IEventRecord, E extends IPersistableEvent> {
    R build(E event);
}
