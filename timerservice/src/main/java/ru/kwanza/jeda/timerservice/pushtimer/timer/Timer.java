package ru.kwanza.jeda.timerservice.pushtimer.timer;

import ru.kwanza.jeda.api.ISink;
import ru.kwanza.jeda.api.timerservice.pushtimer.timer.ITimer;
import ru.kwanza.jeda.api.timerservice.pushtimer.timer.ScheduleTimerEvent;

import java.util.Collection;
import java.util.Map;

/**
 * @author Michael Yeskov
 */
public class Timer implements ITimer {

    @Override
    public void interruptTimers(Collection<String> timerIds) {

    }

    @Override
    public Map<String, Boolean> getIsActiveMap(Collection<String> timerIds) {
        return null;
    }

    @Override
    public boolean isActive(String timerId) {
        return false;
    }

    @Override
    public ISink<ScheduleTimerEvent> getSink() {
        return null;
    }
}
