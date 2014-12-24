package ru.kwanza.jeda.timerservice.entitytimer.entity;

import ru.kwanza.jeda.api.entitytimer.EntityTimer;

/**
 * @author Michael Yeskov
 */
public class EntityWithTimer1 {
    private Long field1;

    @EntityTimer
    public Long timer;

    private Long field2;
}
