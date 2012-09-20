package ru.kwanza.jeda.core.queue;

import ru.kwanza.jeda.api.AbstractEvent;
import ru.kwanza.jeda.api.IEvent;

/**
 * @author Guzanov Alexander
 */
public class Event extends AbstractEvent implements  Cloneable {
    private String contextId;

    public Event(String contextId) {
        this.contextId = contextId;
    }

    public String getContextId() {
        return contextId;
    }

    public Object clone() throws CloneNotSupportedException {
        return new Event(contextId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (contextId != null ? !contextId.equals(event.contextId) : event.contextId != null) return false;

        return true;
    }
}
