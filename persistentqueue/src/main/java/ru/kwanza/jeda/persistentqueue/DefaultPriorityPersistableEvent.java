package ru.kwanza.jeda.persistentqueue;

import ru.kwanza.jeda.api.AbstractEvent;
import ru.kwanza.jeda.api.IPriorityEvent;

/**
 * @author Alexander Guzanov
 */
public class DefaultPriorityPersistableEvent extends DefaultPersistableEvent implements IPriorityPersistableEvent {
    private transient Priority priority;

    public DefaultPriorityPersistableEvent(Long persistId, Priority priority) {
        super(persistId);
        this.priority = priority;
    }

    public DefaultPriorityPersistableEvent(Priority priority) {
        this.priority = priority;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}
