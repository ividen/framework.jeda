package ru.kwanza.jeda.timerservice.pushtimer.spring;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.IEventProcessor;

import java.util.Collection;

/**
 * @author Michael Yeskov
 */
public class PrintProcessor implements IEventProcessor<IEvent> {

    @Override
    public void process(Collection<IEvent> events) {
        System.out.println(events.size());
    }
}
