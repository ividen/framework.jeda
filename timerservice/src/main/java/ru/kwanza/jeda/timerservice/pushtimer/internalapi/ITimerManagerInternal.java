package ru.kwanza.jeda.timerservice.pushtimer.internalapi;

import ru.kwanza.jeda.api.pushtimer.manager.ITimerManager;
import ru.kwanza.jeda.api.pushtimer.manager.NewTimer;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClass;
import ru.kwanza.jeda.timerservice.pushtimer.TimerEntity;
import ru.kwanza.jeda.timerservice.pushtimer.tx.Tx;
import ru.kwanza.txn.api.Transactional;
import ru.kwanza.txn.api.TransactionalType;

import java.util.*;

/**
 * @author Michael Yeskov
 */
public interface ITimerManagerInternal extends ITimerManager{

    public void beforeTrxCommit(Tx tx, Map<TimerClass, Set<NewTimer>> schedule, Map<TimerClass, Set<NewTimer>> reSchedule);

    public void afterTrxCommit(Tx tx, boolean success);

    public List<TimerEntity> getReadyForFireTimers(String timerName, Set<String> timerIds);

    @Transactional(TransactionalType.MANDATORY)
    public void markFiredWithOptLock(String timerName, List<TimerEntity> timersWithState);

}
