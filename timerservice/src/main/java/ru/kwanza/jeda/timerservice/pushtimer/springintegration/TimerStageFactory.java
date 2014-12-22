package ru.kwanza.jeda.timerservice.pushtimer.springintegration;

import ru.kwanza.jeda.api.IStage;
import ru.kwanza.jeda.api.timerservice.pushtimer.manager.ITimerManager;
import ru.kwanza.jeda.core.springintegration.SystemStageFactory;
import ru.kwanza.jeda.core.stage.Stage;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClass;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClassRepository;
import ru.kwanza.jeda.timerservice.pushtimer.timer.Timer;

/**
 * @author Michael Yeskov
 */
public class TimerStageFactory extends SystemStageFactory {
    private TimerClass timerClass;
    private TimerClassRepository timerClassRepository;
    private ITimerManager timerManager;

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

    public ITimerManager getTimerManager() {
        return timerManager;
    }

    public void setTimerManager(ITimerManager timerManager) {
        this.timerManager = timerManager;
    }

    @Override
    public IStage getObject() throws Exception {
        timerClassRepository.registerNameToClassBinding(name, timerClass);

        Timer result = new Timer(manager, timerManager, name, eventProcessor,
                queue, threadManager, admissionController, resourceController, hasTransaction);

        return manager.registerTimer(name, result);
    }
}
