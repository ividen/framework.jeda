package ru.kwanza.jeda.mock;

import ru.kwanza.jeda.api.IEventProcessor;
import ru.kwanza.jeda.api.IFlowBus;
import ru.kwanza.jeda.api.internal.*;

/**
 * @author Guzanov Alexander
 */
public class MockStage implements IStageInternal {

    private String name;
    private MockSink sink = new MockSink();

    public MockStage(String name) {
        this.name = name;
    }

    public IThreadManager getThreadManager() {
        throw new UnsupportedOperationException();
    }

    public IQueue getQueue() {
        throw new UnsupportedOperationException();
    }

    public IAdmissionController getAdmissionController() {
        throw new UnsupportedOperationException();
    }

    public IFlowBus getFlowBus() {
        return MockSystemManager.getInstance().getFlowBus(name);
    }

    public IEventProcessor getProcessor() {
        throw new UnsupportedOperationException();
    }

    public boolean hasTransaction() {
        throw new UnsupportedOperationException();
    }

    public String getName() {
        return name;
    }

    public MockSink getSink() {
        return sink;
    }

    public IResourceController getResourceController() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
