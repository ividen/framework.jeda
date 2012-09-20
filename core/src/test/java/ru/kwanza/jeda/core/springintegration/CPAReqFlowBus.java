package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.IFlowBus;
import ru.kwanza.jeda.api.SinkException;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class CPAReqFlowBus implements IFlowBus {
    public void put(Collection events) throws SinkException {

    }

    public Collection tryPut(Collection events) throws SinkException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
