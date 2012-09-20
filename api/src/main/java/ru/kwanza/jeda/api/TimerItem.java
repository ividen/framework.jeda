package ru.kwanza.jeda.api;

/**
 * @author Guzanov Alexander
 */
public class TimerItem<E extends IEvent> {
    private E event;
    private long millis;
    private String timerHandle;

    public TimerItem(E event, long millis) {
        this.event = event;
        this.millis = millis;
    }

    public TimerItem(E event, long millis, String timerHandle) {
        this.event = event;
        this.millis = millis;
        this.timerHandle = timerHandle;
    }

    public E getEvent() {
        return event;
    }

    public long getMillis() {
        return millis;
    }

    public String getTimerHandle() {
        if (timerHandle == null) {
            throw new IllegalStateException("Timer item is not scheduled!");
        }
        return timerHandle;
    }

    public void setTimerHandle(String handle) {
        this.timerHandle = handle;
    }
}
