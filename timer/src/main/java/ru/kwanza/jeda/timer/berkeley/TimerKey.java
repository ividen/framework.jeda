package ru.kwanza.jeda.timer.berkeley;

import java.io.Serializable;

/**
 * @autor Sergey Shurinov 07.03.12 17:56
 */
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
