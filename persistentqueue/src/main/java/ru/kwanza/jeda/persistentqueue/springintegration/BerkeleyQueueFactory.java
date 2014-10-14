package ru.kwanza.jeda.persistentqueue.springintegration;

import ru.kwanza.jeda.api.internal.IJedaManagerInternal;
import ru.kwanza.jeda.jeconnection.JEConnectionFactory;
import ru.kwanza.jeda.persistentqueue.PersistentQueue;
import ru.kwanza.jeda.persistentqueue.berkeley.BerkeleyQueuePersistenceController;
import org.springframework.beans.factory.SmartFactoryBean;

/**
 * @author Guzanov Alexander
 */
class BerkeleyQueueFactory implements SmartFactoryBean<PersistentQueue> {
    private String dbName;
    private long maxSize;
    private JEConnectionFactory connectionFactory;
    private IJedaManagerInternal manager;

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setConnectionFactory(JEConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
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
