package ru.kwanza.jeda.api.helper;

import ru.kwanza.jeda.api.*;
import ru.kwanza.jeda.api.timerservice.pushtimer.timer.ITimer;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class SinkResolver<E extends IEvent> implements ISink<E> {

    private final IJedaManager manager;
    private ISink<E> original;
    private String name;

    public SinkResolver(IJedaManager manager,String name) {
        this.name = name;
        this.manager = manager;
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

        Object obj = manager.resolveObject(name);
        if (obj instanceof ISink) {
            original = (ISink) obj;
        } else if (obj instanceof IStage) {
            original = ((IStage) obj).getSink();
        } else if (obj instanceof ITimer) {
            original = (ISink)((ITimer) obj).getSink();
        } else {
            throw new SinkException("Unresolved sink!");
        }
    }

}
