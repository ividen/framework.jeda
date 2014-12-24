package ru.kwanza.jeda.timerservice.entitytimer.entity;

import ru.kwanza.jeda.api.entitytimer.EntityTimer;

/**
 * @author Michael Yeskov
 */
public class EntityDuplicateTimerOnProperty1Parent {

    @EntityTimer(name = "timer2")
    private Long timer1;
}
