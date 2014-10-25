package ru.kwanza.jeda.api.internal;

/**
 * @author Guzanov Alexander
 */
public interface IQueueObserver {
    public void notifyChange(int queueSize, int delta);
}
