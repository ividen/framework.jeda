package ru.kwanza.jeda.api.timerservice.pushtimer.timer;

import ru.kwanza.jeda.api.AbstractEvent;

/**
 * @author Michael Yeskov
 */
public class ScheduleTimerEvent extends AbstractEvent{

    private String timerId;
    private long timeoutMS;
    private boolean reSchedule = false;

    public ScheduleTimerEvent(String timerId, long timeoutMS) {
        this.timerId = timerId;
        this.timeoutMS = timeoutMS;
    }

    public ScheduleTimerEvent(String timerId, long timeoutMS, boolean reSchedule) {
        this.timerId = timerId;
        this.timeoutMS = timeoutMS;
        this.reSchedule = reSchedule;
    }

    public String getTimerId() {
        return timerId;
    }

    public long getTimeoutMS() {
        return timeoutMS;
    }

    public boolean isReSchedule() {
        return reSchedule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScheduleTimerEvent)) return false;

        ScheduleTimerEvent that = (ScheduleTimerEvent) o;

        if (!timerId.equals(that.timerId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return timerId.hashCode();
    }

    @Override
    public String toString() {
        return "ScheduleTimerEvent{" +
                "timerId='" + timerId + '\'' +
                ", timeoutMS=" + timeoutMS +
                ", reSchedule=" + reSchedule +
                '}';
    }
}
