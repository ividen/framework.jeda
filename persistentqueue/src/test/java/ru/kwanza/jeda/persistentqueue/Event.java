package ru.kwanza.jeda.persistentqueue;

import ru.kwanza.jeda.api.AbstractEvent;
import ru.kwanza.jeda.api.IEvent;

/**
 * @author Guzanov Alexander
 */
public class Event extends AbstractEvent {
    private String contextId;

    public Event(String contextId) {
        this.contextId = contextId;
    }

    public String getContextId() {
        return contextId;
    }
}
