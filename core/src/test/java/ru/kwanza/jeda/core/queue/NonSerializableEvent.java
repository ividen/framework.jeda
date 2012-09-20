package ru.kwanza.jeda.core.queue;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.toolbox.attribute.AttributeHolder;

/**
 * @author Guzanov Alexander
 */
class NonSerializableEvent implements IEvent {
    private String contextId;
    private AttributeHolder attributes = new AttributeHolder();

    public NonSerializableEvent(String contextId) {
        this.contextId = contextId;
    }

    public String getContextId() {
        return contextId;
    }

    public AttributeHolder getAttributes() {
        return attributes;
    }
}

