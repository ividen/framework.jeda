package ru.kwanza.jeda.timerservice.pushtimer.manual;

import ru.kwanza.jeda.api.IEventProcessor;
import ru.kwanza.jeda.timerservice.pushtimer.StatisticsCalculator;

import java.util.Collection;

/**
 * @author Michael Yeskov
 */
public class Processor implements IEventProcessor<Event> {

    @Override
    public void process(Collection<Event> events) {
        StatisticsCalculator.process.register(events.size());
    }
}
