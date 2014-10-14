package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.IFlowBus;
import ru.kwanza.jeda.api.ISystemManager;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.SmartFactoryBean;
import ru.kwanza.jeda.api.internal.ISystemManagerInternal;

/**
 * @author Guzanov Alexander
 */
public class SystemFlowBusFactory implements SmartFactoryBean<IFlowBus>, BeanNameAware {
    private IFlowBus original;
    private ISystemManagerInternal manager;
    private String name;

    public boolean isPrototype() {
        return false;
    }

    public boolean isEagerInit() {
        return true;
    }

    public IFlowBus getObject() throws Exception {
        return manager.registerFlowBus(name, original);
    }

    public Class<?> getObjectType() {
        return IFlowBus.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void setBeanName(String name) {
        this.name = name;
    }

    public void setOriginal(IFlowBus original) {
        this.original = original;
    }

    public void setManager(ISystemManagerInternal manager) {
        this.manager = manager;
    }
}
