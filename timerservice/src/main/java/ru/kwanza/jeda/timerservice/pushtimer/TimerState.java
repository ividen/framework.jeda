package ru.kwanza.jeda.timerservice.pushtimer;

/**
 * @author Michael Yeskov
 */
public enum TimerState {

    UNKNOWN(0), ACTIVE(1), INTERRUPTED(2),  FIRED(3);

    private long id;

    TimerState(long id) {
        this.id = id;

    }

    public long getId(){
        return id;
    }

    public static TimerState byId(long id){
        if (id == 0) { return UNKNOWN; }
        if (id == 1) { return ACTIVE; }
        if (id == 2) { return INTERRUPTED; }
        if (id == 3) { return FIRED; }
        throw new IllegalArgumentException("Unknown timer id");
    }
}
