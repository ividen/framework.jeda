package ru.kwanza.jeda.api.internal;

import ru.kwanza.jeda.api.*;
import ru.kwanza.jeda.api.pushtimer.ITimer;

/**
 * @author Alexander Guzanov
 */
public interface IJedaManagerInternal extends IJedaManager {
    IStageInternal getStageInternal(String name);

    IContextController registerContextController(String name, IContextController contextController);

    IFlowBus registerFlowBus(String name, IFlowBus flowBus);

    IStage registerStage(IStageInternal stage);

    ITimer registerTimer(String name, ITimerInternal timer);

    void registerObject(String name, Object object);

    void setCurrentStage(IStageInternal stage);

     IStageInternal getCurrentStage();
}
