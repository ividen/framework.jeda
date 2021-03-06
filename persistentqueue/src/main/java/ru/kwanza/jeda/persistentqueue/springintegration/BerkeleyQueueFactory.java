package ru.kwanza.jeda.persistentqueue.springintegration;

import org.springframework.beans.factory.SmartFactoryBean;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.clusterservice.IClusterService;
import ru.kwanza.jeda.jeconnection.JEConnectionFactory;
import ru.kwanza.jeda.persistentqueue.PersistentQueue;
import ru.kwanza.jeda.persistentqueue.berkeley.BerkeleyQueuePersistenceController;

/**
 * @author Guzanov Alexander
 */
class BerkeleyQueueFactory implements SmartFactoryBean<PersistentQueue> {
    private String dbName;
    private int maxSize;
    private JEConnectionFactory connectionFactory;
    private IJedaManager manager;
    private IClusterService service;

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

    public IClusterService getService() {
        return service;
    }

    public void setService(IClusterService service) {
        this.service = service;
    }

    public boolean isPrototype() {
        return false;
    }

    public boolean isEagerInit() {
        return true;
    }

    public PersistentQueue getObject() throws Exception {
        return new PersistentQueue(manager, service, maxSize, new BerkeleyQueuePersistenceController(dbName, connectionFactory));
    }

    public Class<?> getObjectType() {
        return PersistentQueue.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
