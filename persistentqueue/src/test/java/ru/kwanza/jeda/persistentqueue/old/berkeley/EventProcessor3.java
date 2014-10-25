package ru.kwanza.jeda.persistentqueue.old.berkeley;

import ru.kwanza.jeda.api.IEventProcessor;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class EventProcessor3 implements IEventProcessor<TestEvent> {
    public void process(Collection<TestEvent> events) {
        System.out.println(events);
    }
}
