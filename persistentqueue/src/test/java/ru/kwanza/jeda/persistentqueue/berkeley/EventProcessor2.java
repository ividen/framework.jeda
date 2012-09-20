package ru.kwanza.jeda.persistentqueue.berkeley;

import ru.kwanza.jeda.api.IEventProcessor;
import ru.kwanza.jeda.api.Manager;
import ru.kwanza.jeda.api.SinkException;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class EventProcessor2 implements IEventProcessor<TestEvent> {
    public void process(Collection<TestEvent> events) {
        try {
            Manager.getStage("Stage-3").<TestEvent>getSink().put(events);
        } catch (SinkException e) {
            throw new RuntimeException(e);
        }

    }
}