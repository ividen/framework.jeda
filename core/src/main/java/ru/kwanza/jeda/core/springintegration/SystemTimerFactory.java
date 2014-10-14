package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.IContextController;
import ru.kwanza.jeda.api.ITimer;
import ru.kwanza.jeda.api.ISystemManager;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.SmartFactoryBean;
import ru.kwanza.jeda.api.internal.ISystemManagerInternal;

/**
 * @author Guzanov Alexander
 */
class SystemTimerFactory implements SmartFactoryBean<ITimer>, BeanNameAware {
    private ITimer original;
    private ISystemManagerInternal manager;
    private String name;

    public boolean isPrototype() {
        return false;
    }

    public boolean isEagerInit() {
        return true;
    }

    public ITimer getObject() throws Exception {
        return manager.registerTimer(name, original);
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

    public void setOriginal(ITimer original) {
        this.original = original;
    }

    public void setManager(ISystemManagerInternal manager) {
        this.manager = manager;
    }
}
