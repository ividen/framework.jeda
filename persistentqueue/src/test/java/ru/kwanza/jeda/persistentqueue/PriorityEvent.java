package ru.kwanza.jeda.persistentqueue;

import ru.kwanza.jeda.api.IPriorityEvent;

/**
 * @author Guzanov Alexander
 */
public class PriorityEvent extends Event implements IPriorityEvent {
    private Priority priority;

    public PriorityEvent(String contextId) {
        super(contextId);
        this.priority = Priority.NORMAL;
    }

    public PriorityEvent(String contextId, Priority priority) {
        super(contextId);
        this.priority = priority;
    }

    public Priority getPriority() {
        return priority;
    }
}
