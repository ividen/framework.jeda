package ru.kwanza.jeda.timerservice.pushtimer.sink;

import ru.kwanza.jeda.api.ISink;
import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.api.pushtimer.manager.ITimerCreator;
import ru.kwanza.jeda.api.pushtimer.ScheduleTimerEvent;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Michael Yeskov
 */
public class ScheduleTimerSink implements ISink<ScheduleTimerEvent>{

    private String timerName;
    private ITimerCreator timerCreator;

    public ScheduleTimerSink(String timerName, ITimerCreator timerCreator) {
        this.timerName = timerName;
        this.timerCreator = timerCreator;
    }

    @Override
    public void put(Collection<ScheduleTimerEvent> events) throws SinkException {
        timerCreator.processScheduleTimerEvents(timerName, events);
    }

    @Override
    public Collection<ScheduleTimerEvent> tryPut(Collection<ScheduleTimerEvent> events) throws SinkException {
        timerCreator.processScheduleTimerEvents(timerName, events);
        return new ArrayList<ScheduleTimerEvent>();
    }
}
