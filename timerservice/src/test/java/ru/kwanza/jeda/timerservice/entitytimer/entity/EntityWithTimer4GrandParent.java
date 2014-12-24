package ru.kwanza.jeda.timerservice.entitytimer.entity;

import ru.kwanza.jeda.api.entitytimer.EntityTimer;

/**
 * @author Michael Yeskov
 */
public class EntityWithTimer4GrandParent {

    private Long timer5;

    @EntityTimer(name = "timer5")
    public Long getTimer5() {
        return timer5;
    }

    public void setTimer5(Long timer5) {
        this.timer5 = timer5;
    }

    @EntityTimer(name = "timer6")
    private Long timer6;



    public Long timer7;


    protected Long timer8F;

    public Long getTimer8() {
        return timer8F;
    }

    public void setTimer8(Long timer8) {
        this.timer8F = timer8;
    }
}
