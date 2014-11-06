package ru.kwanza.jeda.persistentqueue.berkeley;

import ru.kwanza.jeda.api.AbstractEvent;
import ru.kwanza.jeda.persistentqueue.DefaultPersistableEvent;

/**
 * @author Guzanov Alexander
 */
public class TestEvent extends DefaultPersistableEvent {
    private String contextId;

    public TestEvent(Long persistId, String contextId) {
        super(persistId);
        this.contextId = contextId;
    }

    public String getContextId() {
        return contextId;
    }

    @Override
    public String toString() {
        return "TestEvent{" +
                "contextId='" + contextId + '\'' +
                '}';
    }
}
