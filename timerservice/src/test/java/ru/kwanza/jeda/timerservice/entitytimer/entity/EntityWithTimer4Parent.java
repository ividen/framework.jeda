package ru.kwanza.jeda.timerservice.entitytimer.entity;

import ru.kwanza.jeda.api.timerservice.entitytimer.EntityTimer;

/**
 * @author Michael Yeskov
 */
public class EntityWithTimer4Parent extends EntityWithTimer4GrandParent {
    @EntityTimer(name = "timer1")
    private Long timer1;


    private Long notTimer;


    @EntityTimer(name = "timer2")
    private Long timer2;


    private Long timer3;
    @EntityTimer(name = "timer3")
    public Long getTimer3(){
        return  timer3;
    }
    public void setTimer3(Long timer3){
        this.timer3 = timer3;
    }


    private Long timer4;
    @EntityTimer(name = "timer4")
    public Long getTimer4(){
        return  timer4;
    }

    public void setTimer4(Long timer4){
        this.timer4 = timer4;
    }







}
