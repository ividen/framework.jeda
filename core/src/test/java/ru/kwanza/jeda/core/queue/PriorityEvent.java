package ru.kwanza.jeda.core.queue;

import ru.kwanza.jeda.api.AbstractEvent;
import ru.kwanza.jeda.api.IPriorityEvent;

/**
 * @author Guzanov Alexander
 */
class PriorityEvent  extends AbstractEvent implements IPriorityEvent, Cloneable {
    private String contextId;
    private Priority p;

    public PriorityEvent(String contextId) {
        this(contextId, Priority.NORMAL);
    }

    public PriorityEvent(String contextId, Priority p) {
        this.contextId = contextId;
        this.p = p;
    }

    public String getContextId() {
        return contextId;
    }

    public Priority getPriority() {
        return p;
    }

    public Object clone() throws CloneNotSupportedException {
        return new Event(contextId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PriorityEvent event = (PriorityEvent) o;

        if (contextId != null ? !contextId.equals(event.contextId) : event.contextId != null) return false;

        return true;
    }
}
