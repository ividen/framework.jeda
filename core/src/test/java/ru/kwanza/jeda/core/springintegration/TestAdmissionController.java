package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.api.internal.IAdmissionController;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class TestAdmissionController implements IAdmissionController {
    public Collection tryAccept(Collection events) {
        return null;
    }

    public void accept(Collection events) throws SinkException.Clogged {

    }

    public void adjust(double processingRate, double currentRate) {
    }

    public void degrade(Collection degradeEvents) {

    }
}
