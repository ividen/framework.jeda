package ru.kwanza.jeda.persistentqueue;

import ru.kwanza.jeda.api.AbstractEvent;
import ru.kwanza.jeda.api.IPriorityEvent;

/**
 * @author Alexander Guzanov
 */
public class DefaultPersistableEvent extends AbstractEvent implements IPersistableEvent {
    private transient Long persistId;

    public DefaultPersistableEvent(Long persistId) {
        this.persistId = persistId;
    }

    public DefaultPersistableEvent() {
    }

    public Long getPersistId() {
        return persistId;
    }

    public void setPersistId(Long persistId) {
        this.persistId = persistId;
    }
}
