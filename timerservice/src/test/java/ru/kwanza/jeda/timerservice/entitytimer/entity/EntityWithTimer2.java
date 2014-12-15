package ru.kwanza.jeda.timerservice.entitytimer.entity;

import ru.kwanza.jeda.api.timerservice.entitytimer.EntityTimer;

/**
 * @author Michael Yeskov
 */
public class EntityWithTimer2 {

    @EntityTimer
    private Long timer;

    public Long internalGetTimer(){
        return timer;
    }
}
