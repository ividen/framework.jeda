package ru.kwanza.jeda.timerservice.pushtimer.dao.basis;

import ru.kwanza.dbtool.core.UpdateException;
import ru.kwanza.jeda.timerservice.pushtimer.TimerEntity;

import java.util.Set;

/**
 * @author Michael Yeskov
 */
public class InsertSingleUpdateDBTimerDAO extends AbstractDBTimerDAO {

    @Override
    public void scheduleTimers(Set<TimerEntity> timers, long bucketId) throws UpdateException {
        insertTimers(timers, bucketId); //because we can't reuse timer id in this dao - all duplicate keys means violation of contract. So we throw Exception.
    }

    @Override
    public void reScheduleTimers(Set<TimerEntity> timers, long bucketId) throws UpdateException {
        throw new UnsupportedOperationException("This ADO can create timers only with unique id. So reSchedule operation is not applicable.");

    }
}
