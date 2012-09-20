package ru.kwanza.jeda.api.helper;

import ru.kwanza.jeda.api.*;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class SinkResolver<E extends IEvent> implements ISink<E> {

    private ISink<E> original;
    private String name;

    public SinkResolver(String name) {
        this.name = name;
    }

    public void put(Collection<E> events) throws SinkException {
        checkSink();
        original.put(events);
    }

    public Collection<E> tryPut(Collection<E> events) throws SinkException {
        checkSink();
        return original.tryPut(events);
    }

    public String getName() {
        return name;
    }

    private void checkSink() throws SinkException {
        if (original != null) {
            return;
        }

        Object obj = Manager.resolveObject(name);
        if (obj instanceof ISink) {
            original = (ISink) obj;
        } else if (obj instanceof IStage) {
            original = ((IStage) obj).getSink();
        } else {
            throw new SinkException("Unresolved sink!");
        }
    }

}
