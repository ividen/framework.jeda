package ru.kwanza.jeda.timerservice.entitytimer;

import ru.kwanza.jeda.api.timerservice.entitytimer.IEntityTimerManager;
import ru.kwanza.toolbox.fieldhelper.Property;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Michael Yeskov
 */
public class EntityTimerManager implements IEntityTimerManager {

    @Resource(name = "timerservice.TimersRegistry")
    private ITimersRegistry timersRegistry;

    @Override
    public void registerInfiniteTimer(Object... entityWithTimer) {
        registerTimerWithExpireTime(DEFAULT_TIMER, INFINITE_TIMER_VALUE, entityWithTimer);
    }

    @Override
    public void registerInfiniteTimer(String timerName, Object... entityWithTimer) {
        registerTimerWithExpireTime(timerName, INFINITE_TIMER_VALUE, entityWithTimer);

    }

    @Override
    public void registerTimer(long timeout, Object... entityWithTimer) {
        registerTimerWithExpireTime(DEFAULT_TIMER, System.currentTimeMillis() + timeout, entityWithTimer);
    }

    @Override
    public void registerTimer(String timerName, long timeout, Object... entityWithTimer) {
        registerTimerWithExpireTime(timerName, System.currentTimeMillis() + timeout, entityWithTimer);
    }

    @Override
    public void registerTimerWithExpireTime(long expireTime, Object... entityWithTimer) {
        registerTimerWithExpireTime(DEFAULT_TIMER, expireTime, entityWithTimer);

    }

    @Override
    public void interruptTimer(Object... entityWithTimer) {
        interruptTimer(DEFAULT_TIMER, entityWithTimer);
    }

    @Override
    public boolean isActive(Object entityWithTimer) {
        return isActive(DEFAULT_TIMER, entityWithTimer);
    }




    @Override
    public void registerTimerWithExpireTime(String timerName, long expireTime, Object... entityWithTimer) {
        List<EntityTimerMapping> timerMappings = timersRegistry.getTimerMappings(timerName, entityWithTimer);
        for (int i=0; i< entityWithTimer.length; i++) {
            timerMappings.get(i).getEntityProperty().set(entityWithTimer[i], expireTime);
        }
    }

    @Override
    public void interruptTimer(String timerName, Object... entityWithTimer) {
        List<EntityTimerMapping> timerMappings = timersRegistry.getTimerMappings(timerName, entityWithTimer);
        for (int i=0; i< entityWithTimer.length; i++) {
            timerMappings.get(i).getEntityProperty().set(entityWithTimer[i], null);
        }
    }

    @Override
    public boolean isActive(String timerName, Object entityWithTimer) {
        List<EntityTimerMapping> timerMappings = timersRegistry.getTimerMappings(timerName, entityWithTimer);
        Property entityProperty = timerMappings.get(0).getEntityProperty();
        Long oldValue = (Long)entityProperty.value(entityWithTimer);
        if (INFINITE_TIMER_VALUE.equals(oldValue)) {
            return true;
        }
        if ((oldValue != null) && (oldValue < System.currentTimeMillis())) {
            entityProperty.set(entityWithTimer, null);
            oldValue = null;
        }
        return oldValue != null;
    }
}
