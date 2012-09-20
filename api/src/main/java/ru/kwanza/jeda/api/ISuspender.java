package ru.kwanza.jeda.api;

import java.util.Collection;

/**
 * @author Dmitry Zagorovsky
 */
public interface ISuspender<E extends IEvent> {

    public E suspend(ISink<E> sink, E event);

    public E suspend(String sinkName, E event);

    public Collection<E> suspend(ISink<E> sink, Collection<E> events);

    public Collection<E> suspend(String sinkName, Collection<E> events);

    public void flush() throws SuspendException;

}
