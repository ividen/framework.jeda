package ru.kwanza.jeda.api;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public interface IEventProcessor<E extends IEvent> {

    public void process(Collection<E> events);
}
