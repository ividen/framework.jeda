package ru.kwanza.jeda.context.springintegration;

import ru.kwanza.dbtool.core.VersionGenerator;
import ru.kwanza.jeda.api.IContextController;
import ru.kwanza.jeda.api.internal.ISystemManager;
import ru.kwanza.jeda.jeconnection.JEConnectionFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.SmartFactoryBean;

abstract class AbstractBerkeleyBlobContextControllerFactory
        implements SmartFactoryBean<IContextController>, BeanNameAware {

    protected ISystemManager manager;
    protected String name;

    protected String databaseName;
    protected JEConnectionFactory connectionFactory;
    protected VersionGenerator versionGenerator;
    protected String terminator;

    public void setBeanName(String name) {
        this.name = name;
    }

    public boolean isPrototype() {
        return false;
    }

    public boolean isEagerInit() {
        return true;
    }

    public boolean isSingleton() {
        return true;
    }

    public void setManager(ISystemManager manager) {
        this.manager = manager;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public void setConnectionFactory(JEConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void setVersionGenerator(VersionGenerator versionGenerator) {
        this.versionGenerator = versionGenerator;
    }

    public void setTerminator(String terminator) {
        this.terminator = terminator;
    }

}
