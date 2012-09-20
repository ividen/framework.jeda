package ru.kwanza.jeda.api.internal;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.ISink;

/**
 * @author Guzanov Alexander
 */
public interface IQueue<E extends IEvent> extends ISource<E>, ISink<E> {
    public void setObserver(IQueueObserver observer);

    public IQueueObserver getObserver();

    public int getEstimatedCount();

    public boolean isReady();
}
