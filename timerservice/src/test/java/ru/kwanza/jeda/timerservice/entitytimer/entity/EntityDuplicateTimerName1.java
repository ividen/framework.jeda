package ru.kwanza.jeda.timerservice.entitytimer.entity;

import ru.kwanza.jeda.api.entitytimer.EntityTimer;

/**
 * @author Michael Yeskov
 */
public class EntityDuplicateTimerName1 {
    private Long notTimer;
    @EntityTimer
    private Long timer1;
    private Long notTimer2;
    @EntityTimer
    private Long timer2;

}
