package ru.kwanza.jeda.persistentqueue.db.queue;

import ru.kwanza.jeda.persistentqueue.DefaultPersistableEvent;
import ru.kwanza.jeda.persistentqueue.db.IDBQueueHelper;
import ru.kwanza.jeda.persistentqueue.db.base.BaseEventQueue;
import ru.kwanza.toolbox.fieldhelper.FieldHelper;

/**
 * @author Alexander Guzanov
 */
public abstract class BaseEventQueueHelper<Q extends BaseEventQueue, E extends DefaultPersistableEvent>
        implements IDBQueueHelper<Q, E> {
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

    public FieldHelper.Field<Q, E> getEvent() {
        final FieldHelper.Field event = FieldHelper.<BaseEventQueue,E>construct(BaseEventQueue.class, "event");
        return event;
    }
}
