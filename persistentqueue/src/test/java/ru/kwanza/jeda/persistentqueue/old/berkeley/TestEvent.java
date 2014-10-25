package ru.kwanza.jeda.persistentqueue.old.berkeley;

import ru.kwanza.jeda.api.AbstractEvent;

/**
 * @author Guzanov Alexander
 */
public class TestEvent extends AbstractEvent {
    private String contextId;

    public TestEvent(String contextId) {
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
