package ru.kwanza.jeda.api.internal;

import ru.kwanza.jeda.api.IEventProcessor;
import ru.kwanza.jeda.api.IStage;

/**
 * @author Guzanov Alexander
 */
public interface IStageInternal extends IStage {

    public IThreadManager getThreadManager();

    public IQueue getQueue();

    public IAdmissionController getAdmissionController();

    public IEventProcessor getProcessor();

    public boolean hasTransaction();

    public IResourceController getResourceController();
}
