package ru.kwanza.jeda.api.internal;

import ru.kwanza.jeda.api.IEvent;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public interface ISource<E extends IEvent> {
    public Collection<E> take(int count) throws SourceException;

    public int size();
}
