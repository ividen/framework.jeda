package ru.kwanza.jeda.timerservice.pushtimer.timer;

import ru.kwanza.jeda.api.ISink;
import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.api.pushtimer.manager.ITimerManager;
import ru.kwanza.jeda.api.pushtimer.manager.TimerHandle;
import ru.kwanza.jeda.api.pushtimer.ITimer;
import ru.kwanza.jeda.api.pushtimer.ScheduleTimerEvent;
import ru.kwanza.jeda.timerservice.pushtimer.Helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Yeskov
 */
public class TimerBatcher implements ITimer {

    private TimerSink timerSink;
    private String name;
    private ITimerManager timerManager;

    public TimerBatcher(String name, ITimerManager timerManager) {
        timerSink = new TimerSink();
        this.name = name;
        this.timerManager = timerManager;
    }

    @Override
    public void interruptTimers(Collection<String> timerIds) {
        timerManager.interruptTimers(Helper.toTimerHandles(name, timerIds));
    }

    @Override
    public Map<String, Boolean> getIsActiveMap(Collection<String> timerIds) {
        Map<TimerHandle, Boolean> wrappedResult =  timerManager.getIsActiveMap(Helper.toTimerHandles(name, timerIds));
        Map<String, Boolean> result = new HashMap<String, Boolean>();
        for (Map.Entry<TimerHandle, Boolean> entry : wrappedResult.entrySet()) {
            result.put(entry.getKey().getTimerId(), entry.getValue());
        }
        return result;
    }

    @Override
    public boolean isActive(String timerId) {
        return timerManager.isActive(new TimerHandle(name, timerId));
    }

    @Override
    public ISink getSink() {
        return timerSink;
    }

    @Override
    public String getName() {
        return name;
    }

    private class TimerSink implements ISink<ScheduleTimerEvent>{
        @Override
        public void put(Collection<ScheduleTimerEvent> events) throws SinkException {
            timerManager.processScheduleTimerEvents(name, events);
        }

        @Override
        public Collection<ScheduleTimerEvent> tryPut(Collection<ScheduleTimerEvent> events) throws SinkException {
            timerManager.processScheduleTimerEvents(name, events);
            return null;
        }
    }
}
