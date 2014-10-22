package ru.kwanza.jeda.persistentqueue.berkeley;

import ru.kwanza.jeda.api.IEventProcessor;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.SinkException;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class EventProcessor2 implements IEventProcessor<TestEvent> {
    private IJedaManager sm;
    public void process(Collection<TestEvent> events) {
        try {
            sm.getStage("Stage-3").<TestEvent>getSink().put(events);
        } catch (SinkException e) {
            throw new RuntimeException(e);
        }

    }
}