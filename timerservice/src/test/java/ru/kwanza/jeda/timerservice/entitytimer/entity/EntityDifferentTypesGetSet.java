package ru.kwanza.jeda.timerservice.entitytimer.entity;

import ru.kwanza.jeda.api.entitytimer.EntityTimer;

/**
 * @author Michael Yeskov
 */
public class EntityDifferentTypesGetSet {

    private Long field;

    @EntityTimer
    public Long getField() {
        return field;
    }


    public void setField(String field) {
        //
    }
}
