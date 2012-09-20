package ru.kwanza.jeda.timer;

import ru.kwanza.jeda.api.AbstractEvent;

/**
 * @autor Sergey Shurinov 05.03.12 16:59
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
