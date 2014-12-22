package ru.kwanza.jeda.core.manager;

import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.api.internal.IStageInternal;
import ru.kwanza.jeda.api.timerservice.internal.ITimerInternal;
import ru.kwanza.jeda.api.timerservice.pushtimer.timer.ITimer;

import java.util.Collection;
import java.util.Map;

/**
 * @author Michael Yeskov
 */
public class SystemTimer extends SystemStage implements ITimer{

    private ITimerInternal timerInternal;

    public SystemTimer(ITimerInternal timerInternal) {
        super(timerInternal);
        this.timerInternal = timerInternal;
    }

    @Override
    public void interruptTimers(Collection<String> timerIds) {
        timerInternal.interruptTimers(timerIds);
    }

    @Override
    public Map<String, Boolean> getIsActiveMap(Collection<String> timerIds) {
        return timerInternal.getIsActiveMap(timerIds);
    }

    @Override
    public boolean isActive(String timerId) {
        return timerInternal.isActive(timerId);
    }
}
