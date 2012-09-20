package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.IEventProcessor;
import ru.kwanza.jeda.api.IStage;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class TestEventProcessor1 implements IEventProcessor {
    private IStage nextStage;

    public IStage getNextStage() {
        return nextStage;
    }

    public void setNextStage(IStage nextStage) {
        this.nextStage = nextStage;
    }

    public void process(Collection events) {

    }
}
