package ru.kwanza.jeda.api.internal;

import ru.kwanza.jeda.api.*;

/**
 * @author Guzanov Alexander
 */
public interface ISystemManager {
    public IContextController getContextController(String name);

    public IStageInternal getCurrentStage();

    public IFlowBus getFlowBus(String name);

    public IStage getStage(String name);

    public IStageInternal getStageInternal(String name);

    public ITimer getTimer(String name);

    public IPendingStore getPendingStore();

    public ITransactionManagerInternal getTransactionManager();

    public IContextController registerContextController(String name, IContextController contextController);

    public IFlowBus registerFlowBus(String name, IFlowBus flowBus);

    public IStage registerStage(IStageInternal stage);

    public ITimer registerTimer(String name, ITimer timer);

    public void registerObject(String name, Object object);

    public String resolveObjectName(Object object);

    public <OBJ> OBJ resolveObject(String objectName);

    public void setCurrentStage(IStageInternal stage);
}
