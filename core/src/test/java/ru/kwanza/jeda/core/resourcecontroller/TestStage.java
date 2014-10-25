package ru.kwanza.jeda.core.resourcecontroller;

import ru.kwanza.jeda.api.*;
import ru.kwanza.jeda.api.internal.*;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class TestStage implements IStageInternal, IQueue {


    private int estiatedCount;

    public IThreadManager getThreadManager() {
        throw new UnsupportedOperationException();
    }

    public IQueue getQueue() {
        return this;
    }

    public IAdmissionController getAdmissionController() {
        throw new UnsupportedOperationException();
    }

    public IFlowBus getFlowBus() {
        throw new UnsupportedOperationException();
    }

    public IEventProcessor getProcessor() {
        throw new UnsupportedOperationException();
    }

    public boolean hasTransaction() {
        throw new UnsupportedOperationException();
    }

    public IResourceController getResourceController() {
        throw new UnsupportedOperationException();
    }

    public String getName() {
        return "Test";
    }

    public <E extends IEvent> ISink<E> getSink() {
        throw new UnsupportedOperationException();
    }

    public void setObserver(IQueueObserver observer) {
        throw new UnsupportedOperationException();
    }

    public IQueueObserver getObserver() {
        throw new UnsupportedOperationException();
    }

    public int getEstimatedCount() {
        return estiatedCount;
    }

    public boolean isReady() {
        return true;
    }

    public void setEstiatedCount(int estiatedCount) {
        this.estiatedCount = estiatedCount;
    }

    public void put(Collection events) throws SinkException {
        throw new UnsupportedOperationException();
    }

    public Collection tryPut(Collection events) throws SinkException {
        throw new UnsupportedOperationException();
    }

    public Collection take(int count) throws SourceException {
        throw new UnsupportedOperationException();
    }

    public int size() {
        throw new UnsupportedOperationException();
    }
}
