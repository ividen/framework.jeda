package ru.kwanza.jeda.timerservice.entitytimer.entity;

import ru.kwanza.jeda.api.entitytimer.EntityTimer;

/**
 * @author Michael Yeskov
 */
public class EntityDuplicateTimerOnProperty2Parent {
    private Long field;

    @EntityTimer(name = "timer2")
    public Long getTimer1(){
        return field;
    }


    public void setTimer1(Long timer1){
        this.field = timer1;
    }


}
