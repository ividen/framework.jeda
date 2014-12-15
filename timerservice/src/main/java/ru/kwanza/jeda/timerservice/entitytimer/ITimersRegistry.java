package ru.kwanza.jeda.timerservice.entitytimer;

import java.util.List;

/**
 * @author Michael Yeskov
 */
public interface ITimersRegistry {

    public List<TimerMapping> getTimerMappings(String timerName, Object... entityWithTimer);

}
