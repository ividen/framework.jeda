package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.IContextController;
import ru.kwanza.jeda.api.internal.ISystemManager;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.SmartFactoryBean;

/**
 * @author Guzanov Alexander
 */
class SystemContextControllerFactory implements SmartFactoryBean<IContextController>, BeanNameAware {
    private IContextController original;
    private ISystemManager manager;
    private String name;

    public boolean isPrototype() {
        return false;
    }

    public boolean isEagerInit() {
        return true;
    }

    public IContextController getObject() throws Exception {
        return manager.registerContextController(name, original);
    }

    public Class<?> getObjectType() {
        return IContextController.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void setBeanName(String name) {
        this.name = name;
    }

    public void setOriginal(IContextController original) {
        this.original = original;
    }

    public void setManager(ISystemManager manager) {
        this.manager = manager;
    }
}
