package ru.kwanza.jeda.api.pushtimer.manager;

/**
 * @author Michael Yeskov
 */
public class TimerHandle implements Comparable<TimerHandle>{

    private String timerName;
    private String timerId;

    public TimerHandle(String timerName, String timerId) {
        this.timerName = timerName;
        this.timerId = timerId;
    }


    public String getTimerName() {
        return timerName;
    }

    public void setTimerName(String timerName) {
        this.timerName = timerName;
    }


    public String getTimerId() {
        return timerId;
    }

    public void setTimerId(String timerId) {
        this.timerId = timerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimerHandle)) return false;

        TimerHandle that = (TimerHandle) o;

        if (!timerId.equals(that.timerId)) return false;
        if (!timerName.equals(that.timerName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = timerName.hashCode();
        result = 31 * result + timerId.hashCode();
        return result;
    }

    @Override
    public int compareTo(TimerHandle o) {
        int c = timerName.compareTo(o.getTimerName());
        if (c != 0 ) {
            return c;
        }
        return timerId.compareTo(o.getTimerId());
    }

    @Override
    public String toString() {
        return "TimerHandle{" +
                "timerName='" + timerName + '\'' +
                ", timerId='" + timerId + '\'' +
                '}';
    }
}
