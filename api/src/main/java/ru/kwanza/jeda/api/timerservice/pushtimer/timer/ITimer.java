package ru.kwanza.jeda.api.timerservice.pushtimer.timer;

import ru.kwanza.jeda.api.ISink;
import ru.kwanza.txn.api.Transactional;
import ru.kwanza.txn.api.TransactionalType;

import java.util.Collection;
import java.util.Map;

/**
 * Helper class for using ITimerManager methods in context of constant TimerName
 * with JEDA-friendly interface.
 * @author Michael Yeskov
 */
public interface ITimer {

    /*
     * Interrupts timers in DB that was created in earlie
     * Timer record in db stays locked until transaction completion
     */
    @Transactional(TransactionalType.REQUIRED)
    public void interruptTimers(Collection<String> timerIds);

    /*
     * Checks timer state in DB
     * Active trx changes are not visible for this method
     */
    public Map<String, Boolean> getIsActiveMap (Collection<String> timerIds);


    /*
     * Checks timer state in DB
     * Active trx changes are not visible for this method
     * Not batch variant use only if performance doesn't matter.
     */
    public boolean isActive(String timerId);



    /*
     * Event style api for timer creation
     * Friendly with SinkHelper
     */
    public ISink<ScheduleTimerEvent> getSink();


    public String getName();

}
