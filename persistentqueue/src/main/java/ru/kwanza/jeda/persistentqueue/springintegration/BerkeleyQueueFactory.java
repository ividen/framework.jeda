package ru.kwanza.jeda.persistentqueue.springintegration;

import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.jeconnection.JEConnectionFactory;

import org.springframework.beans.factory.SmartFactoryBean;
import ru.kwanza.jeda.persistentqueue.PersistentQueue;
import ru.kwanza.jeda.persistentqueue.old.berkeley.BerkeleyQueuePersistenceController;

/**
 * @author Guzanov Alexander
 */
class BerkeleyQueueFactory implements SmartFactoryBean<PersistentQueue> {
    private String dbName;
    private int maxSize;
    private JEConnectionFactory connectionFactory;
    private IJedaManager manager;

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setConnectionFactory(JEConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void setManager(IJedaManager manager) {
        this.manager = manager;
    }

    public boolean isPrototype() {
        return false;
    }

    public boolean isEagerInit() {
        return true;
    }

    public PersistentQueue getObject() throws Exception {
        return new PersistentQueue(manager, maxSize, new BerkeleyQueuePersistenceController(dbName, connectionFactory));
    }

    public Class<?> getObjectType() {
        return PersistentQueue.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
