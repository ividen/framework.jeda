package ru.kwanza.jeda.api.timerservice.pushtimer.manager;

import ru.kwanza.jeda.api.timerservice.pushtimer.timer.ScheduleTimerEvent;
import ru.kwanza.txn.api.Transactional;
import ru.kwanza.txn.api.TransactionalType;

import java.util.Collection;


/**
 * Deferred timer creation
 * Timer will be crated at commit time.
 * All checks will be performed on commit.
 * If any check fails transaction will be rolled back.
 *
 * @author Michael Yeskov
 */
public interface ITimerCreator {

    /*
     * Schedule timer if current state is not active.
     * If timer already registered in this tx  then exception will be thrown immediate.
     * If has duplicates in supplied collection exception will be thrown immediate.
     */
    @Transactional(TransactionalType.REQUIRED)
    public void scheduleTimers(Collection<NewTimer> timers);

    /*
     * Schedule timer if current state is not active.
     * If timer already registered in this tx  then exception will be thrown immediate.
     * If has duplicates in supplied collection exception will be thrown immediate.
     */
    @Transactional(TransactionalType.REQUIRED)
    public void scheduleTimers(long timeoutMS, Collection<TimerHandle> timerHandles);


    @Transactional(TransactionalType.REQUIRED)
    public void scheduleTimers(String timerName, long timeoutMS, Collection<String> timerIds);


    /*
     * Schedule new timer regardless of current timer state
     * If timer already registered in this tx previous value will be lost
     * If has duplicates in supplied collection exception will be thrown immediate.
     */
    @Transactional(TransactionalType.REQUIRED)
    public void reScheduleTimers(Collection<NewTimer> timers);

    /*
     * Schedule new timer regardless of current timer state
     * If timer already registered in this tx previous value will be lost
     * If has duplicates in supplied collection exception will be thrown immediate.
     */
    @Transactional(TransactionalType.REQUIRED)
    public void reScheduleTimers(long timeoutMS, Collection<TimerHandle> timerHandles);


    @Transactional(TransactionalType.REQUIRED)
    public void reScheduleTimers(String timerName, long timeoutMS, Collection<String> timerIds);


    @Transactional(TransactionalType.REQUIRED)
    public void processScheduleTimerEvents(String timerName, Collection<ScheduleTimerEvent> scheduleTimerEvents);


    /*
     * Removes timers from current Tx synchronization
     */

    public  void cancelJustScheduledTimers(Collection<? extends TimerHandle> timerHandles);
}
