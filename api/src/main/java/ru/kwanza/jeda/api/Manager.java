package ru.kwanza.jeda.api;

import ru.kwanza.jeda.api.internal.ISystemManager;
import ru.kwanza.txn.api.spi.ITransactionManager;

import javax.annotation.Resource;

/**
 * @author Guzanov Alexander
 */
public class Manager {
    public final static Manager instance = new Manager();

    @Resource(name = "ru.kwanza.jeda.api.internal.ISystemManager")
    private ISystemManager systemManager;

    public static Manager getInstance() {
        return instance;
    }

    public static ITransactionManager getTM() {
        return instance.getTM0();
    }

    public static IStage getStage(String path) {
        return instance.getStage0(path);
    }

    public static ITimer getTimer(String name) {
        return instance.getTimer0(name);
    }

    public static <ID, C extends IContext<ID, ?>> IContextController<ID, C> getContextController(String name) {
        return (IContextController<ID, C>) instance.getContextController0(name);
    }

    public static IFlowBus getFlowBus(String name) {
        return instance.getFlowBus0(name);
    }

    public static IStage getCurrentStage() {
        return instance.getCurrentStage0();
    }

    public static String resolveObjectName(Object object) {
        return instance.resolveObjectName0(object);
    }

    public static <OBJ> OBJ resolveObject(String name) {
        return instance.<OBJ>resolveObject0(name);
    }

    public static IPendingStore getPendingStore() {
        return instance.getPendingStore0();
    }

    protected ISystemManager getSystemManager() {
        return systemManager;
    }


    @Override
    public String toString() {
        return "Manager{" +
                "systemManager=" + getSystemManager() +
                '}';
    }

    protected IContextController getContextController0(String name) {
        return getSystemManager().getContextController(name);
    }

    protected IStage getCurrentStage0() {
        return getSystemManager().getCurrentStage();
    }

    protected IFlowBus getFlowBus0(String name) {
        IFlowBus flowBus = getSystemManager().getFlowBus(name);
        return flowBus;
    }

    protected IStage getStage0(String path) {
        return getSystemManager().getStage(path);
    }

    protected ITransactionManager getTM0() {
        return getSystemManager().getTransactionManager();
    }

    protected ITimer getTimer0(String name) {
        return getSystemManager().getTimer(name);
    }

    protected IPendingStore getPendingStore0() {
        return getSystemManager().getPendingStore();
    }

    private String resolveObjectName0(Object object) {
        return getSystemManager().resolveObjectName(object);
    }

    private <OBJ> OBJ resolveObject0(String name) {
        return getSystemManager().<OBJ>resolveObject(name);
    }
}
