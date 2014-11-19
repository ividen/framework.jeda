package ru.kwanza.jeda.persistentqueue.db.queue;

import ru.kwanza.jeda.persistentqueue.DefaultPersistableEvent;
import ru.kwanza.jeda.persistentqueue.db.IEventRecordHelper;
import ru.kwanza.jeda.persistentqueue.db.base.BaseEventQueue;

/**
 * @author Alexander Guzanov
 */
public abstract class BaseEventQueueHelper<Q extends BaseEventQueue, E extends DefaultPersistableEvent>
        implements IEventRecordHelper<Q, E> {
    public String getQueueNameField() {
        return null;
    }

    public String getQueueNameValue() {
        return null;
    }

    public String getIdField() {
        return "id";
    }

    public String getNodeIdField() {
        return "nodeId";
    }
}
