package ru.kwanza.jeda.timerservice.entitytimer.entity;

import ru.kwanza.jeda.api.entitytimer.EntityTimer;

/**
 * @author Michael Yeskov
 */
public class EntityDuplicateTimerName4Parent {
    private Long timer2;

    @EntityTimer(name = "timer")
    public Long getTimer2() {
        return timer2;
    }

    public void setTimer2(Long timer2) {
        this.timer2 = timer2;
    }
}
