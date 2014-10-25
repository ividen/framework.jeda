package ru.kwanza.jeda.persistentqueue;

import ru.kwanza.jeda.api.AbstractEvent;
import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.IPriorityEvent;
import ru.kwanza.toolbox.fieldhelper.FieldHelper;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class EventWithKey extends AbstractEvent implements IPriorityEvent {
    private IEvent delegate;
    private Object key;

    public static <E extends IEvent> Collection<E> extract(final Collection<EventWithKey> events) {
        return FieldHelper.getFieldCollection(events, FieldHelper.<EventWithKey, E>construct(EventWithKey.class, "delegate"));
    }

    public EventWithKey(IEvent delegate) {
        this.delegate = delegate;
    }

    public EventWithKey(Object key, IEvent delegate) {
        if (key == null) {
            throw new NullPointerException("Key can not be null");
        }
        this.key = key;
        this.delegate = delegate;
    }

    public IEvent getDelegate() {
        return delegate;
    }

    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public Priority getPriority() {
        return ((IPriorityEvent) delegate).getPriority();
    }
}
