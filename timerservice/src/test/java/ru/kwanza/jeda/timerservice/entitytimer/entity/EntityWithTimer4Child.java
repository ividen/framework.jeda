package ru.kwanza.jeda.timerservice.entitytimer.entity;

import ru.kwanza.jeda.api.timerservice.entitytimer.EntityTimer;

/**
 * @author Michael Yeskov
 */
public class EntityWithTimer4Child extends EntityWithTimer4Parent {
    private Long field1;

    private Long field2;

    private Long field3;

    @EntityTimer(name="timer7")
    private Long timer7;

    @EntityTimer(name="timer8")
    public Long getTimer8(Long timer8){
        return timer8F;
    }
}
