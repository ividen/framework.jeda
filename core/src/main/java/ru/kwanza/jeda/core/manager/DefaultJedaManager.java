package ru.kwanza.jeda.core.manager;

import ru.kwanza.jeda.api.*;
import ru.kwanza.jeda.api.internal.IJedaManagerInternal;
import ru.kwanza.jeda.api.internal.IStageInternal;
import ru.kwanza.jeda.api.internal.ITransactionManagerInternal;
import ru.kwanza.jeda.api.internal.ITimerInternal;
import ru.kwanza.jeda.api.pushtimer.ITimer;
import ru.kwanza.jeda.core.stage.SystemQueue;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Guzanov Alexander
 */
public class DefaultJedaManager implements IJedaManagerInternal {
    public ThreadLocal<IStageInternal> currentStage = new ThreadLocal<IStageInternal>();
    private ConcurrentMap<String, IContextController> contextControllers = new ConcurrentHashMap<String, IContextController>();

    private ConcurrentMap<String, SystemFlowBus> flowBuses = new ConcurrentHashMap<String, SystemFlowBus>();
    private ConcurrentMap<String, SystemStage> stages = new ConcurrentHashMap<String, SystemStage>();
    private ConcurrentMap<String, Object> objects = new ConcurrentHashMap<String, Object>();
    private ConcurrentMap<Object, String> names = new ConcurrentHashMap<Object, String>();
    private IPendingStore pendingStore;
    private ITransactionManagerInternal tm;


    public ITransactionManagerInternal getTransactionManager() {
        return tm;
    }

    public SystemStage getStage(String name) {
        SystemStage stage = stages.get(name);
        if (stage == null) {
            throw new ObjectNotFoundException("Stage \"" + name + "\" not found");
        }
        return stage;
    }

    public IStageInternal getStageInternal(String name) {
        return getStage(name).unwrap();
    }

    public SystemTimer getTimer(String name) {
        IStage timer = stages.get(name);
        if (timer == null ||  (!(timer instanceof ITimer))) {
            throw new ObjectNotFoundException("Timer \"" + name + "\" not found");
        }
        return (SystemTimer)timer;
    }

    public IPendingStore getPendingStore() {
        return pendingStore;
    }

    public IContextController getContextController(String name) {
        IContextController context = contextControllers.get(name);
        if (context == null) {
            throw new ObjectNotFoundException("Context \"" + name + "\" not found");
        }

        return context;
    }

    public IFlowBus getFlowBus(String name) {
        IFlowBus flowBus = flowBuses.get(name);
        if (flowBus == null) {
            throw new ObjectNotFoundException("FlowBus \"" + name + "\" not found");
        }
        return flowBus;
    }

    public IStageInternal getCurrentStage() {
        return currentStage.get();
    }

    public void setCurrentStage(IStageInternal stage) {
        currentStage.set(stage);
    }

    public IContextController registerContextController(String name, IContextController context) {
        SystemContextController value = new SystemContextController(name, context);
        if (null == contextControllers.putIfAbsent(name, value)) {
            return value;
        }
        return contextControllers.get(name);
    }

    public IFlowBus registerFlowBus(String name, IFlowBus flowBus) {
        SystemFlowBus value = new SystemFlowBus(name, flowBus);
        if (null == flowBuses.putIfAbsent(name, value)) {
            return value;
        }

        return flowBuses.get(name);
    }

    public IStage registerStage(IStageInternal stage) {
        SystemStage value = new SystemStage(stage);
        if (null == stages.putIfAbsent(stage.getName(), value)) {
            return value;
        }

        return stages.get(stage.getName());
    }

    @Override
    public ITimer registerTimer(String name, ITimerInternal timer) {
        SystemTimer value = new SystemTimer(timer);
        if (null == stages.putIfAbsent(name, value)) {
            return value;
        }
        return (ITimer)stages.get(name);
    }

    public void registerObject(String name, Object object) {
        if (null == objects.putIfAbsent(name, object)) {
            names.put(object, name);
        }
    }

    public String resolveObjectName(Object object) {
        if (object instanceof SystemFlowBus) {
            return ((SystemFlowBus) object).getName();
        } else if (object instanceof SystemQueue.SystemSink) {
            return ((SystemQueue.SystemSink) object).getName();
        } else if (object instanceof SystemContextController) {
            return ((SystemContextController) object).getName();
        } else if (object instanceof IStage) {
            return ((IStage) object).getName();
        } else {
            return names.get(object);
        }
    }

    public Object resolveObject(String objectName) {
        Object result = stages.get(objectName);
        if (result != null) return result;
        result = flowBuses.get(objectName);
        if (result != null) return result;
        result = contextControllers.get(objectName);
        if (result != null) return result;
        result = objects.get(objectName);

        return result;
    }

    public void setPendingStore(IPendingStore pendingStore) {
        this.pendingStore = pendingStore;
    }

    public void setTransactionManager(ITransactionManagerInternal tm) {
        this.tm = tm;
    }

}
