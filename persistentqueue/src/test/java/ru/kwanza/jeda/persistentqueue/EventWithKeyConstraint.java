package ru.kwanza.jeda.persistentqueue;

import ru.kwanza.jeda.api.IEvent;

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
