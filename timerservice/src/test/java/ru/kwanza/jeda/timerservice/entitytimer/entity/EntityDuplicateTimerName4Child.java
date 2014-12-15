package ru.kwanza.jeda.timerservice.entitytimer.entity;

import ru.kwanza.jeda.api.timerservice.entitytimer.EntityTimer;

/**
 * @author Michael Yeskov
 */
public class EntityDuplicateTimerName4Child extends EntityDuplicateTimerName4Parent {

    private Long timer1;


    @EntityTimer(name = "timer")
    public Long getTimer1() {
        return timer1;
    }


    public void setTimer1(Long timer1) {
        this.timer1 = timer1;
    }
}
