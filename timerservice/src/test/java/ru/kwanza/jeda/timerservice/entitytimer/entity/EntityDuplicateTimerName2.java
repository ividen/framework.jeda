package ru.kwanza.jeda.timerservice.entitytimer.entity;

import ru.kwanza.jeda.api.entitytimer.EntityTimer;

/**
 * @author Michael Yeskov
 */
public class EntityDuplicateTimerName2 {
    @EntityTimer(name = "timer")
    private Long timer1;

    @EntityTimer(name = "timer")
    private Long timer2;
}
