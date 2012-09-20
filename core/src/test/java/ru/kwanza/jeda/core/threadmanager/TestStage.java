package ru.kwanza.jeda.core.threadmanager;

import ru.kwanza.jeda.api.IEventProcessor;
import ru.kwanza.jeda.api.IFlowBus;
import ru.kwanza.jeda.api.ISink;
import ru.kwanza.jeda.api.internal.*;

/**
 * @author Guzanov Alexander
 */
public class TestStage implements IStageInternal {
    private String name;

    public TestStage(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ISink getSink() {
        return null;
    }

    public IThreadManager getThreadManager() {
        return null;
    }

    public IQueue getQueue() {
        return null;
    }

    public IAdmissionController getAdmissionController() {
        return null;
    }

    public IFlowBus getFlowBus() {
        return null;
    }

    public IEventProcessor getProcessor() {
        return null;
    }

    public boolean hasTransaction() {
        return false;
    }

    public IResourceController getResourceController() {
        return null;
    }
}
