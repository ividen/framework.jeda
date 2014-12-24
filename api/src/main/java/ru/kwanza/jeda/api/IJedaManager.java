package ru.kwanza.jeda.api;

import ru.kwanza.jeda.api.internal.ITransactionManagerInternal;
import ru.kwanza.jeda.api.pushtimer.ITimer;

/**
 * @author Guzanov Alexander
 */
public interface IJedaManager {

    <ID, C extends IContext<ID, ?>> IContextController<ID, C> getContextController(String name);

    IStage getCurrentStage();

    IFlowBus getFlowBus(String name);

    IStage getStage(String name);

    ITimer getTimer(String name);

    ITransactionManagerInternal getTransactionManager();

    IPendingStore getPendingStore();

    String resolveObjectName(Object object);

    <OBJ> OBJ resolveObject(String objectName);
}
