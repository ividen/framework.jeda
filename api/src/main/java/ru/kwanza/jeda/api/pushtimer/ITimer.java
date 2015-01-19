package ru.kwanza.jeda.api.pushtimer;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.kwanza.jeda.api.IStage;

import java.util.Collection;
import java.util.Map;

/**
 * Helper class for using ITimerManager methods in context of constant TimerName
 * with JEDA-friendly interface.
 * @author Michael Yeskov
 */
public interface ITimer extends IStage {

    /*
     * Interrupts timers in DB that was created in earlie
     * Timer record in db stays locked until transaction completion
     */
    @Transactional(propagation = Propagation.REQUIRED)
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
     * Removes timers from current Tx synchronization
     */

    public  void cancelJustScheduledTimers(Collection<String> timerIds);
}
