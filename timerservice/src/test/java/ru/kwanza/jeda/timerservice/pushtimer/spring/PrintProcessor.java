package ru.kwanza.jeda.timerservice.pushtimer.spring;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.IEventProcessor;
import ru.kwanza.jeda.api.timerservice.pushtimer.processor.TimerFiredEvent;
import ru.kwanza.jeda.core.manager.SystemStage;

import java.util.Collection;

/**
 * @author Michael Yeskov
 */
public class PrintProcessor implements IEventProcessor<TimerFiredEvent> {

    @Override
    public void process(Collection<TimerFiredEvent> events) {
        /*
        System.out.println(events.size());
        for (TimerFiredEvent event : events) {
            System.out.println("Fired!!! timer=" + event.getTimerName() + " id=" + event.getTimerId());
        }
        */
    }
}
