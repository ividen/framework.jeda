package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.api.internal.IQueueObserver;
import ru.kwanza.jeda.api.internal.SourceException;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class TestQueue1 implements IQueue {
    private String param1;
    private String param2;

    public TestQueue1(String param1, String param2) {
        this.param1 = param1;
        this.param2 = param2;
    }

    public void setObserver(IQueueObserver observer) {
    }

    public IQueueObserver getObserver() {
        return null;
    }

    public int getEstimatedCount() {
        return 0;
    }

    public boolean isReady() {
        return true;
    }

    public void put(Collection events) throws SinkException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Collection tryPut(Collection events) throws SinkException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Collection take(int count) throws SourceException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int size() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
