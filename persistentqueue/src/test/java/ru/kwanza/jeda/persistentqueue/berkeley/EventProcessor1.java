package ru.kwanza.jeda.persistentqueue.berkeley;

import ru.kwanza.jeda.api.IEventProcessor;
import ru.kwanza.jeda.api.ISystemManager;
import ru.kwanza.jeda.api.SinkException;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class EventProcessor1 implements IEventProcessor<TestEvent> {
    private ISystemManager sm;
    public void process(Collection<TestEvent> events) {
        try {
            sm.getStage("Stage-2").<TestEvent>getSink().put(events);
        } catch (SinkException e) {
            throw new RuntimeException(e);
        }

    }
}
