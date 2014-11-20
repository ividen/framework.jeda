package ru.kwanza.jeda.persistentqueue.springintegration;

import org.springframework.beans.factory.SmartFactoryBean;
import ru.kwanza.dbtool.orm.api.IEntityManager;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.clusterservice.IClusterService;
import ru.kwanza.jeda.persistentqueue.PersistentQueue;
import ru.kwanza.jeda.persistentqueue.db.DBQueuePersistenceController;
import ru.kwanza.jeda.persistentqueue.db.IDBQueueHelper;

/**
 * @author Guzanov Alexander
 */
class DBQueueFactory implements SmartFactoryBean<PersistentQueue> {
    private int maxSize;
    private IDBQueueHelper recordHelper;
    private IJedaManager manager;
    private IEntityManager em;
    private IClusterService clusterService;


    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public IDBQueueHelper getRecordHelper() {
        return recordHelper;
    }

    public void setRecordHelper(IDBQueueHelper recordHelper) {
        this.recordHelper = recordHelper;
    }

    public IJedaManager getManager() {
        return manager;
    }

    public void setManager(IJedaManager manager) {
        this.manager = manager;
    }

    public IEntityManager getEm() {
        return em;
    }

    public void setEm(IEntityManager em) {
        this.em = em;
    }

    public IClusterService getClusterService() {
        return clusterService;
    }

    public void setClusterService(IClusterService clusterService) {
        this.clusterService = clusterService;
    }

    public boolean isPrototype() {
        return false;
    }

    public boolean isEagerInit() {
        return true;
    }

    public PersistentQueue getObject() throws Exception {
        DBQueuePersistenceController controller = new DBQueuePersistenceController(em,recordHelper);

        return new PersistentQueue(manager,clusterService, maxSize, controller);
    }

    public Class<?> getObjectType() {
        return PersistentQueue.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
