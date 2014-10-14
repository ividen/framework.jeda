package ru.kwanza.jeda.api;

import ru.kwanza.jeda.api.internal.ITransactionManagerInternal;

/**
 * @author Guzanov Alexander
 */
public interface IJedaManager {
    IContextController getContextController(String name);

    IStage getCurrentStage();

    IFlowBus getFlowBus(String name);

    IStage getStage(String name);

    ITimer getTimer(String name);

    ITransactionManagerInternal getTransactionManager();

    IPendingStore getPendingStore();

    String resolveObjectName(Object object);

    <OBJ> OBJ resolveObject(String objectName);
}
