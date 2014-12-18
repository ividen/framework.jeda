package ru.kwanza.jeda.timerservice.entitytimer;

import java.util.List;

/**
 * @author Michael Yeskov
 */
public interface ITimersRegistry {

    public List<EntityTimerMapping> getTimerMappings(String timerName, Object... entityWithTimer);

}
