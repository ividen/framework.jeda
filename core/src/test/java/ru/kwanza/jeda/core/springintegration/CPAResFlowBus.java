package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.IContextController;
import ru.kwanza.jeda.api.IFlowBus;
import ru.kwanza.jeda.api.Manager;
import ru.kwanza.jeda.api.SinkException;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class CPAResFlowBus implements IFlowBus {
    private IFlowBus parent;
    private IContextController controller;

    public IFlowBus getParent() {
        return parent;
    }

    public void setParent(IFlowBus parent) {
        this.parent = parent;
    }

    public void setController(IContextController controller) {
        this.controller = controller;

    }

    public void put(Collection events) throws SinkException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Collection tryPut(Collection events) throws SinkException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
