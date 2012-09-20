package ru.kwanza.jeda.api.internal;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.SinkException;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public interface IAdmissionController<E extends IEvent> {
    public Collection<E> tryAccept(Collection<E> events);

    public void accept(Collection<E> events) throws SinkException.Clogged;

    public void adjust(double processingRate, double currentRate);

    public void degrade(Collection<E> degradeEvents);
}
