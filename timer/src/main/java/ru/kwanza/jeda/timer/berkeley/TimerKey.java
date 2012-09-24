package ru.kwanza.jeda.timer.berkeley;

import java.io.Serializable;


public class TimerKey implements Serializable {

    private long id;
    private long millis;

    public TimerKey(long id, long millis) {
        this.id = id;
        this.millis = millis;
    }

    public long getId() {
        return id;
    }

    public long getMillis() {
        return millis;
    }
}
