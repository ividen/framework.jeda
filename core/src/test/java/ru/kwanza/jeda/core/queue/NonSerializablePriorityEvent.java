package ru.kwanza.jeda.core.queue;

import ru.kwanza.jeda.api.IPriorityEvent;
import ru.kwanza.toolbox.attribute.AttributeHolder;

/**
 * @author Guzanov Alexander
 */
public class NonSerializablePriorityEvent implements IPriorityEvent {
    private String contextId;
    private IPriorityEvent.Priority p;
    private AttributeHolder attributes = new AttributeHolder();

    public NonSerializablePriorityEvent(String contextId) {
        this(contextId, IPriorityEvent.Priority.NORMAL);
    }

    public NonSerializablePriorityEvent(String contextId, IPriorityEvent.Priority p) {
        this.contextId = contextId;
        this.p = p;
    }

    public String getContextId() {
        return contextId;
    }

    public IPriorityEvent.Priority getPriority() {
        return p;
    }

    public AttributeHolder getAttributes() {
        return attributes;
    }
}
