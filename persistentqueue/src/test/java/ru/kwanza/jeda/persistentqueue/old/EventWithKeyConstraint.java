package ru.kwanza.jeda.persistentqueue.old;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.persistentqueue.EventWithKey;

/**
 * @author Ivan Baluk
 */
public class EventWithKeyConstraint extends EventWithKey {
    public EventWithKeyConstraint(IEvent delegate) {
        super(delegate);
    }

    public EventWithKeyConstraint(Object key, IEvent delegate) {
        super(key, delegate);
    }

    @Override
    public Object getKey() {
        return null;
    }
}
