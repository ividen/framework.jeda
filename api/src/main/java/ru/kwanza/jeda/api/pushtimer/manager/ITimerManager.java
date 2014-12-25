package ru.kwanza.jeda.api.pushtimer.manager;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.kwanza.jeda.api.pushtimer.ITimer;

import java.util.Collection;
import java.util.Map;

/**
 * @author Michael Yeskov
 */
public interface ITimerManager extends ITimerCreator {

    /*
     * Interrupts timers in DB that was created in earlie
     * Timer record in db stays locked until transaction completion
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void interruptTimers(Collection<? extends TimerHandle> timerHandles);


    /*
     * Checks timer state in DB
     * Active trx changes are not visible for this method
     */
    public Map<TimerHandle, Boolean> getIsActiveMap (Collection<? extends TimerHandle> timers);


    /*
     * Checks timer state in DB
     * Active trx changes are not visible for this method
     * Not batch variant use only if performance doesn't matter.
     */
    public boolean isActive(TimerHandle timer);


    public ITimer getTimer(String timerName);

}
