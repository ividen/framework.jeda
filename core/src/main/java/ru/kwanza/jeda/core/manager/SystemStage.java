package ru.kwanza.jeda.core.manager;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.ISink;
import ru.kwanza.jeda.api.IStage;
import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.api.internal.IStageInternal;
import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class SystemStage implements IStage, ISink {

    private IStageInternal stageInternal;

    public SystemStage(IStageInternal stageInternal) {
        this.stageInternal = stageInternal;
    }

    public String getName() {
        return stageInternal.getName();
    }

    public <E extends IEvent> ISink<E> getSink() {
        return stageInternal.getSink();
    }

    IStageInternal unwrap() {
        return stageInternal;
    }

    public void put(Collection events) throws SinkException {
        this.stageInternal.getSink().put(events);

    }

    public Collection tryPut(Collection events) throws SinkException {
        return this.stageInternal.getSink().tryPut(events);
    }
}
