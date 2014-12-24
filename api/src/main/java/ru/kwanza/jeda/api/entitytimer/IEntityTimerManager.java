package ru.kwanza.jeda.api.entitytimer;

/**
 * @author Michael Yeskov
 */
public interface IEntityTimerManager {
    public static final String DEFAULT_TIMER = "default";
    public static final Long INFINITE_TIMER_VALUE = -1L;

    void registerInfiniteTimer(Object... entityWithTimer);

    void registerInfiniteTimer(String timerName, Object... entityWithTimer);

    void registerTimer(long timeout, Object... entityWithTimer);

    void registerTimer(String timerName, long timeout, Object... entityWithTimer);

    void registerTimerWithExpireTime(long expireTime, Object... entityWithTimer);

    void registerTimerWithExpireTime(String timerName, long expireTime, Object... entityWithTimer);

    void interruptTimer(Object... entityWithTimer);

    void interruptTimer(String timerName, Object... entityWithTimer);

    boolean isActive(Object entityWithTimer);

    boolean isActive(String timerName, Object entityWithTimer);
}
