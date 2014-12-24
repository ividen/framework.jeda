package ru.kwanza.jeda.api.pushtimer;

import ru.kwanza.jeda.api.AbstractEvent;

/**
 * @author Michael Yeskov
 */
public class TimerFiredEvent extends AbstractEvent {

    private String timerName;
    private String timerId;


    public TimerFiredEvent(String timerName, String timerId) {
        this.timerName = timerName;
        this.timerId = timerId;
    }

    public String getTimerId() {
        return timerId;
    }


    public String getTimerName() {
        return timerName;
    }
}
