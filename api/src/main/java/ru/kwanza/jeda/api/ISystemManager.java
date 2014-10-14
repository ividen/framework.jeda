package ru.kwanza.jeda.api;

/**
 * @author Guzanov Alexander
 */
public interface ISystemManager {
    IContextController getContextController(String name);

    IStage getCurrentStage();

    IFlowBus getFlowBus(String name);

    IStage getStage(String name);

    ITimer getTimer(String name);

    IPendingStore getPendingStore();

    String resolveObjectName(Object object);

    <OBJ> OBJ resolveObject(String objectName);
}
