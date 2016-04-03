package ru.kwanza.jeda.timerservice.pushtimer.tx;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ru.kwanza.jeda.api.pushtimer.ScheduleTimerEvent;
import ru.kwanza.jeda.api.pushtimer.manager.NewTimer;
import ru.kwanza.jeda.api.pushtimer.manager.TimerHandle;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClass;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClassRepository;
import ru.kwanza.jeda.timerservice.pushtimer.internalapi.ITimerManagerInternal;

import java.util.*;

/**
 * @author Michael Yeskov
 */
public class Tx implements TransactionSynchronization {
    private static final Object TX_KEY = new Object();

    private ITimerManagerInternal timerManager;
    private TimerClassRepository repository;

    Map<TimerClass, Set<NewTimer>> pendingSchedule = new HashMap<TimerClass, Set<NewTimer>>();
    Map<TimerClass, Set<NewTimer>> reSchedulePending = new HashMap<TimerClass, Set<NewTimer>>();


    public Tx(ITimerManagerInternal timerManager, TimerClassRepository repository) {
        this.timerManager = timerManager;
        this.repository = repository;
    }


    public static Tx getTx(ITimerManagerInternal timerManager, TimerClassRepository repository) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            Tx result = (Tx) TransactionSynchronizationManager.getResource(TX_KEY);
            if (result == null) {
                result = new Tx(timerManager, repository);
                TransactionSynchronizationManager.bindResource(TX_KEY, result);
                TransactionSynchronizationManager.registerSynchronization(result);

            }
            return result;
        } else {
            return null;
        }
    }

    @Override
    public void suspend() {
        TransactionSynchronizationManager.unbindResourceIfPossible(TX_KEY);
    }

    @Override
    public void resume() {
        TransactionSynchronizationManager.bindResource(TX_KEY, this);
    }

    @Override
    public void flush() {

    }

    @Override
    public void beforeCommit(boolean readonly) {
        timerManager.beforeTrxCommit(this, pendingSchedule, reSchedulePending);
    }

    @Override
    public void beforeCompletion() {
        TransactionSynchronizationManager.unbindResourceIfPossible(TX_KEY);
    }

    @Override
    public void afterCommit() {
    }

    @Override
    public void afterCompletion(int i) {
        timerManager.afterTrxCommit(this, i == TransactionSynchronization.STATUS_COMMITTED);
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
        Set<NewTimer> currentScheduleSet = getRequiredSet(pendingSchedule, timerClass);
        Set<NewTimer> reScheduleSetCurrent = getRequiredSet(reSchedulePending, timerClass);

        if (!reSchedule) {
            if (currentScheduleSet.contains(newTimer) || reScheduleSetCurrent.contains(newTimer)) {
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
            Set<NewTimer> currentScheduleSet = getRequiredSet(pendingSchedule, timerClass);
            Set<NewTimer> reScheduleSetCurrent = getRequiredSet(reSchedulePending, timerClass);
            currentScheduleSet.remove(timerHandle);
            reScheduleSetCurrent.remove(timerHandle);
        }

    }
}

