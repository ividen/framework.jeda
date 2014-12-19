package ru.kwanza.jeda.timerservice.pushtimer.springintegration;

import ru.kwanza.jeda.api.IStage;
import ru.kwanza.jeda.core.springintegration.SystemStageFactory;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClass;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClassRepository;

/**
 * @author Michael Yeskov
 */
public class TimerStageFactory extends SystemStageFactory {
    private TimerClass timerClass;
    private TimerClassRepository timerClassRepository;

    public TimerClass getTimerClass() {
        return timerClass;
    }

    public void setTimerClass(TimerClass timerClass) {
        this.timerClass = timerClass;
    }

    public TimerClassRepository getTimerClassRepository() {
        return timerClassRepository;
    }

    public void setTimerClassRepository(TimerClassRepository timerClassRepository) {
        this.timerClassRepository = timerClassRepository;
    }

    @Override
    public IStage getObject() throws Exception {
        timerClassRepository.registerNameToClassBinding(name, timerClass);
        manager.registerTimer(name); //TODO::::
        return super.getObject();
    }
}
