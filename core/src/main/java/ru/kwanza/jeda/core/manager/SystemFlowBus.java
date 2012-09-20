package ru.kwanza.jeda.core.manager;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.IFlowBus;
import ru.kwanza.jeda.api.SinkException;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class SystemFlowBus<E extends IEvent> implements IFlowBus<E> {
    private IFlowBus<E> flowBus;
    private String name;

    public SystemFlowBus(String name, IFlowBus<E> flowBus) {
        this.name = name;
        this.flowBus = flowBus;
    }

    public void put(Collection<E> events) throws SinkException {
        flowBus.put(events);
    }

    public Collection<E> tryPut(Collection<E> events) throws SinkException {
        return flowBus.tryPut(events);
    }

    public String getName() {
        return name;
    }
}
