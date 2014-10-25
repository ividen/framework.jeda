package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.api.internal.IQueueObserver;
import ru.kwanza.jeda.api.internal.SourceException;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class TestQueue2 implements IQueue {
    private Long p1;
    private Long p2;

    public TestQueue2(Long p1, Long p2) {
        this.p1 = p1;
        this.p2 = p2;
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

    }

    public Collection tryPut(Collection events) throws SinkException {
        return null;
    }

    public Collection take(int count) throws SourceException {
        return null;
    }

    public int size() {
        return 0;
    }

    public static final TestQueue2 create(Long p1, Long p2) {
        return new TestQueue2(p1, p2);
    }
}
