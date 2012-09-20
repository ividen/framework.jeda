package ru.kwanza.jeda.api;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public interface ISink<E extends IEvent> {

    public void put(Collection<E> events) throws SinkException;

    public Collection<E> tryPut(Collection<E> events) throws SinkException;

}
