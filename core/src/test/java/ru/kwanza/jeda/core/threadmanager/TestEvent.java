package ru.kwanza.jeda.core.threadmanager;

import ru.kwanza.jeda.api.AbstractEvent;
import ru.kwanza.jeda.api.IEvent;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Guzanov Alexander
 */
public class TestEvent  extends AbstractEvent {
    private static final AtomicLong ids = new AtomicLong(0l);
    private String contextId;
    private long id = ids.incrementAndGet();

    public TestEvent(String contextId) {
        this.contextId = contextId;
    }

    public String getContextId() {
        return contextId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestEvent testEvent = (TestEvent) o;

        if (id != testEvent.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
