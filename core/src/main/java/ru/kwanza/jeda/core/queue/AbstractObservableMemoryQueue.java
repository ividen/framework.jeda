package ru.kwanza.jeda.core.queue;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.api.internal.IQueueObserver;

/**
 * @author Guzanov Alexander
 */
public abstract class AbstractObservableMemoryQueue<E extends IEvent> implements IQueue<E> {
    private IQueueObserver observer;


    public void setObserver(IQueueObserver observer) {
        this.observer = observer;
    }

    public IQueueObserver getObserver() {
        return observer;
    }

    protected void notify(int size, int delta) {
        if (observer != null && delta != 0) {
            observer.notifyChange(size, delta);
        }
    }
}
