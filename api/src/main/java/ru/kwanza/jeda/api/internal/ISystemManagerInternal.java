package ru.kwanza.jeda.api.internal;

import ru.kwanza.jeda.api.*;

/**
 * @author Alexander Guzanov
 */
public interface ISystemManagerInternal extends ISystemManager {
    IStageInternal getStageInternal(String name);

    ITransactionManagerInternal getTransactionManager();

    IContextController registerContextController(String name, IContextController contextController);

    IFlowBus registerFlowBus(String name, IFlowBus flowBus);

    IStage registerStage(IStageInternal stage);

    ITimer registerTimer(String name, ITimer timer);

    void registerObject(String name, Object object);

    void setCurrentStage(IStageInternal stage);

     IStageInternal getCurrentStage();
}
