package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.IEventProcessor;
import ru.kwanza.jeda.api.IStage;
import ru.kwanza.jeda.api.internal.IAdmissionController;
import ru.kwanza.jeda.api.internal.IJedaManagerInternal;
import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.api.internal.IResourceController;
import ru.kwanza.jeda.core.stage.Stage;
import ru.kwanza.jeda.core.threadmanager.AbstractThreadManager;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.SmartFactoryBean;

/**
 * @author Guzanov Alexander
 */
class SystemStageFactory implements SmartFactoryBean<IStage>, BeanNameAware {
    private IAdmissionController admissionController;
    private IEventProcessor eventProcessor;
    private IQueue queue;
    private IResourceController resourceController;
    private AbstractThreadManager threadManager;
    private IJedaManagerInternal manager;
    private boolean hasTransaction = true;
    private String name;


    public void setHasTransaction(boolean hasTransaction) {
        this.hasTransaction = hasTransaction;
    }

    public void setAdmissionController(IAdmissionController admissionController) {
        this.admissionController = admissionController;
    }

    public void setEventProcessor(IEventProcessor eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    public void setQueue(IQueue queue) {
        this.queue = queue;
    }

    public void setResourceController(IResourceController resourceController) {
        this.resourceController = resourceController;
    }

    public void setThreadManager(AbstractThreadManager threadManager) {
        this.threadManager = threadManager;
    }

    public void setManager(IJedaManagerInternal manager) {
        this.manager = manager;
    }

    public boolean isPrototype() {
        return false;
    }

    public boolean isEagerInit() {
        return true;
    }

    public IStage getObject() throws Exception {
        Stage result = new Stage(manager, name, eventProcessor,
                queue, threadManager, admissionController, resourceController, hasTransaction);

        return manager.registerStage(result);
    }

    public Class<?> getObjectType() {
        return IStage.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void setBeanName(String name) {
        this.name = name;
    }
}
