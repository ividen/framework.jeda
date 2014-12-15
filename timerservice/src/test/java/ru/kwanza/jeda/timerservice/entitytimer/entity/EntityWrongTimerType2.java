package ru.kwanza.jeda.timerservice.entitytimer.entity;

import ru.kwanza.jeda.api.timerservice.entitytimer.EntityTimer;

/**
 * @author Michael Yeskov
 */
public class EntityWrongTimerType2 {

    private String field;

    @EntityTimer
    public String getField() {
        return null;
    }


    public void setField(String field) {

    }
}
