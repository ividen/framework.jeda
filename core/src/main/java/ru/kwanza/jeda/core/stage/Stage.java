package ru.kwanza.jeda.core.stage;

import ru.kwanza.jeda.api.*;
import ru.kwanza.jeda.api.internal.*;
import ru.kwanza.jeda.core.threadmanager.AbstractThreadManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Guzanov Alexander
 */
public class Stage implements IStageInternal, IQueueObserver {

    private static final Logger logger = LoggerFactory.getLogger(Stage.class);
    protected AbstractThreadManager threadManager;
    protected SystemQueue queue;
    protected IResourceController resourceController;
    protected IAdmissionController admissionController;
    protected IEventProcessor processor;
    private String name;
    private boolean hasTransaction;
    private IJedaManagerInternal manager;

    public Stage(IJedaManagerInternal manager, String name, IEventProcessor processor,
            IQueue queue, AbstractThreadManager threadManager, IAdmissionController admissionController,
            IResourceController resourceController, boolean hasTransaction) {
        this.manager = manager;
        this.name = name;
        this.processor = processor;
        this.admissionController = admissionController;
        this.resourceController = resourceController;
        if (resourceController instanceof AbstractResourceController) {
            ((AbstractResourceController) resourceController).initStage(this);
        }
        this.queue = new SystemQueue(queue, admissionController, this);
        this.threadManager = threadManager;
        this.hasTransaction = hasTransaction;
    }

    public void notifyChange(long queueSize, long delta) {
        if (logger.isTraceEnabled()) {
            logger.trace("Queue state of Stage {} is changed.Current size={}, delta={}",
                    new Object[]{getName(), queueSize, delta});
        }
        if (delta > 0) {
            IStageInternal currStage = manager.getCurrentStage();
            manager.setCurrentStage(this);
            try {
                resourceController.input(delta);
                threadManager.adjustThreadCount(this, resourceController.getThreadCount());
            } finally {
                manager.setCurrentStage(currStage);
            }
        }
    }

    public String getName() {
        return name;
    }

    public ISink getSink() {
        return queue.asSink();
    }

    public IThreadManager getThreadManager() {
        return threadManager;
    }

    public IQueue getQueue() {
        return queue;
    }

    public IAdmissionController getAdmissionController() {
        return admissionController;
    }

    public IEventProcessor getProcessor() {
        return processor;
    }

    public boolean hasTransaction() {
        return hasTransaction;
    }

    public IResourceController getResourceController() {
        return resourceController;
    }
}
