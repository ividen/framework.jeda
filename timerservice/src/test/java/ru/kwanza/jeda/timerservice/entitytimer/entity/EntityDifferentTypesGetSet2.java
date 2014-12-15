package ru.kwanza.jeda.timerservice.entitytimer.entity;

import ru.kwanza.jeda.api.timerservice.entitytimer.EntityTimer;

/**
 * @author Michael Yeskov
 */
public class EntityDifferentTypesGetSet2 {


    private Long field2;

    @EntityTimer
    public Long getField() {
        return field2;
    }


    public void setField(String field) {
        //
    }
}
