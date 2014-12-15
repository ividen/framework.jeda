package ru.kwanza.jeda.timerservice.pushtimer.dao;

import ru.kwanza.dbtool.core.UpdateException;
import ru.kwanza.jeda.api.timerservice.pushtimer.manager.TimerHandle;
import ru.kwanza.jeda.timerservice.pushtimer.TimerEntity;

import java.util.*;

/**
 * @author Michael Yeskov
 */
public interface IDBTimerDAO {

    public void scheduleTimers(Set<TimerEntity> timers, long bucketId) throws UpdateException;

    /*
     * Some implementations may throw exception in case of optimistic lock
     * Some are working atomically
     */
    public void reScheduleTimers(Set<TimerEntity> timers, long bucketId) throws UpdateException;

    public void interruptTimers(Set<TimerHandle> timersToInterrupt) throws UpdateException;

    public Map<TimerHandle, Boolean> getIsActiveMap(Collection<TimerHandle> timersToFind);

    public IFetchCursor createFetchCursor(long leftBorder, long rightBorder, long bucketId);

    public List<TimerEntity> loadTimerEntities(String timerName, Set<String> timerIds);

    public void markFiredWithOptLock(String timerName, List<TimerEntity> entities) throws UpdateException;


    public long getFetchSize();

    public Set<String> getCompatibleTimerNames();

}
