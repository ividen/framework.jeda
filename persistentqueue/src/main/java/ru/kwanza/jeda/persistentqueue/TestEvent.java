package ru.kwanza.jeda.persistentqueue;

import ru.kwanza.jeda.api.AbstractEvent;
import ru.kwanza.jeda.api.IEvent;

/**
 * @author Guzanov Alexander
 */
public class TestEvent extends AbstractEvent {
    private String contexId;

    public TestEvent(String contexId) {
        this.contexId = contexId;
    }

    public String getContextId() {
        return contexId;
    }
}
