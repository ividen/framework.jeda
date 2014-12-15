package ru.kwanza.jeda.timerservice.entitytimer.entity;

import ru.kwanza.jeda.api.timerservice.entitytimer.EntityTimer;

/**
 * @author Michael Yeskov
 */
public class EntityDuplicateTimerOnProperty2Child extends EntityDuplicateTimerOnProperty2Parent{
    @EntityTimer(name = "timer1")
    private Long timer1;
}
