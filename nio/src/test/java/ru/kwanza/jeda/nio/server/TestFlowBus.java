package ru.kwanza.jeda.nio.server;

import ru.kwanza.jeda.api.IFlowBus;
import ru.kwanza.jeda.api.IStage;
import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.nio.server.http.IHttpEvent;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class TestFlowBus implements IFlowBus<IHttpEvent> {
    private IStage next;

    public void setNext(IStage next) {
        this.next = next;
    }

    public void put(Collection<IHttpEvent> events) throws SinkException {
       next.<IHttpEvent>getSink().put(events);
    }

    public Collection<IHttpEvent> tryPut(Collection<IHttpEvent> events) throws SinkException {
       return next.<IHttpEvent>getSink().tryPut(events);
    }
}
