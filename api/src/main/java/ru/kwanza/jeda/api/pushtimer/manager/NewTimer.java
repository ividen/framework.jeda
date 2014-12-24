package ru.kwanza.jeda.api.pushtimer.manager;

/**
 * @author Michael Yeskov
 */
public class NewTimer extends TimerHandle {

    private long timeoutMS;

    public NewTimer(String timerName, String timerId, long timeoutMS) {
        super(timerName, timerId);
        this.timeoutMS = timeoutMS;
    }

    public long getTimeoutMS() {
        return timeoutMS;
    }

    public void setTimeoutMS(long timeoutMS) {
        this.timeoutMS = timeoutMS;
    }
}
