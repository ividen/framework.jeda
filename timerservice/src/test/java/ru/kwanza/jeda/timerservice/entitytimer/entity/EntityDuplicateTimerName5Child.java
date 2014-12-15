package ru.kwanza.jeda.timerservice.entitytimer.entity;

import ru.kwanza.jeda.api.timerservice.entitytimer.EntityTimer;

/**
 * @author Michael Yeskov
 */
public class EntityDuplicateTimerName5Child extends EntityDuplicateTimerName5Parent {

    @EntityTimer(name = "timer")
    private Long timer1;
}
