package ru.kwanza.jeda.timerservice.pushtimer.tx;

import ru.kwanza.jeda.api.timerservice.pushtimer.manager.NewTimer;
import ru.kwanza.jeda.api.timerservice.pushtimer.manager.TimerHandle;
import ru.kwanza.jeda.api.timerservice.pushtimer.timer.ScheduleTimerEvent;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClass;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClassRepository;
import ru.kwanza.jeda.timerservice.pushtimer.internalapi.ITimerManagerInternal;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import java.util.*;

/**
 * @author Michael Yeskov
 */
public class Tx implements Synchronization {

    private ITimerManagerInternal timerManager;
    private PendingTxTimersStore txStore;
    private Transaction jtaTrx;
    private TimerClassRepository repository;

    Map<TimerClass, Set<NewTimer>> pendingSchedule = new HashMap<TimerClass, Set<NewTimer>>();
    Map<TimerClass, Set<NewTimer>> reSchedulePending = new HashMap<TimerClass, Set<NewTimer>>();


    public Tx(ITimerManagerInternal timerManager, PendingTxTimersStore txStore, Transaction jtaTrx, TimerClassRepository repository) {
        this.timerManager = timerManager;
        this.txStore = txStore;
        this.jtaTrx = jtaTrx;
        this.repository = repository;
    }

    public void beforeCompletion() {
        timerManager.beforeTrxCommit(this, pendingSchedule, reSchedulePending);
    }

    public void afterCompletion(int i) {
        timerManager.afterTrxCommit(this, i == Status.STATUS_COMMITTED);
        txStore.removeTx(jtaTrx);
    }

    public void processNewTimers(Set<NewTimer> timers, boolean reSchedule) {
        for (NewTimer newTimer : timers) {
            TimerClass timerClass = repository.getClassByTimerName(newTimer.getTimerName());
            internalProcess(timerClass, newTimer, reSchedule);
        }
    }

    public void processScheduleEvents(String timerName, Set<ScheduleTimerEvent> events) {
        TimerClass timerClass = repository.getClassByTimerName(timerName);
        for (ScheduleTimerEvent event : events) {
            NewTimer newTimer = new NewTimer(timerName, event.getTimerId(), event.getTimeoutMS());
            internalProcess(timerClass, newTimer, event.isReSchedule());
        }

    }

    private void internalProcess(TimerClass timerClass, NewTimer newTimer, boolean reSchedule) {
        Set<NewTimer> currentScheduleSet  = getRequiredSet(pendingSchedule, timerClass);
        Set<NewTimer> reScheduleSetCurrent = getRequiredSet(reSchedulePending, timerClass);

        if (!reSchedule) {
            if ( currentScheduleSet.contains(newTimer) || reScheduleSetCurrent.contains(newTimer) ){
                throw new RuntimeException("Timer " + newTimer + " was already scheduled in scope of current trx.");
            }
            currentScheduleSet.add(newTimer);
        } else {
            currentScheduleSet.remove(newTimer);
            reScheduleSetCurrent.remove(newTimer);

            reScheduleSetCurrent.add(newTimer);
        }
    }

    private Set<NewTimer> getRequiredSet(Map<TimerClass, Set<NewTimer>> source, TimerClass timerClass) {
        Set<NewTimer> result = source.get(timerClass);
        if (result == null) {
            result = new HashSet<NewTimer>();
            source.put(timerClass, result);
        }
        return result;
    }


    public void cancelScheduling(Collection<? extends TimerHandle> timerHandles) {
        for (TimerHandle timerHandle : timerHandles) {
            TimerClass timerClass = repository.getClassByTimerName(timerHandle.getTimerName());
            Set<NewTimer> currentScheduleSet  = getRequiredSet(pendingSchedule, timerClass);
            Set<NewTimer> reScheduleSetCurrent = getRequiredSet(reSchedulePending, timerClass);
            currentScheduleSet.remove(timerHandle);
            reScheduleSetCurrent.remove(timerHandle);
        }

    }



}

