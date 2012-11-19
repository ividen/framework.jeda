package ru.kwanza.jeda.persistentqueue.springintegration;

import ru.kwanza.autokey.api.IAutoKey;
import ru.kwanza.dbtool.core.DBTool;
import ru.kwanza.jeda.api.internal.ISystemManager;
import ru.kwanza.jeda.persistentqueue.PersistentQueue;
import ru.kwanza.jeda.persistentqueue.jdbc.JDBCQueuePersistenceController;
import org.springframework.beans.factory.SmartFactoryBean;

/**
 * @author Guzanov Alexander
 */
class JDBCQueueFactory implements SmartFactoryBean<PersistentQueue> {
    private long maxSize;
    private DBTool dbTool;
    private IAutoKey autoKey;
    private String tableName;
    private String idColumn;
    private String eventColumn;
    private String nodeIdColumn;
    private String queueNameColumn;
    private String queueName;
    private ISystemManager manager;

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    public void setDbTool(DBTool dbTool) {
        this.dbTool = dbTool;
    }

    public void setAutoKey(IAutoKey autoKey) {
        this.autoKey = autoKey;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setIdColumn(String idColumn) {
        this.idColumn = idColumn;
    }

    public void setEventColumn(String eventColumn) {
        this.eventColumn = eventColumn;
    }

    public void setNodeIdColumn(String nodeIdColumn) {
        this.nodeIdColumn = nodeIdColumn;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public void setQueueNameColumn(String queueNameColumn) {
        this.queueNameColumn = queueNameColumn;
    }

    public void setManager(ISystemManager manager) {
        this.manager = manager;
    }

    public boolean isPrototype() {
        return false;
    }

    public boolean isEagerInit() {
        return true;
    }

    public PersistentQueue getObject() throws Exception {
        JDBCQueuePersistenceController controller = new JDBCQueuePersistenceController(queueName);
        controller.setDbTool(dbTool);
        controller.setAutoKey(autoKey);
        if (idColumn != null) {
            controller.setIdColumn(idColumn);
        }
        if (eventColumn != null) {
            controller.setEventColumn(eventColumn);
        }

        if (tableName != null) {
            controller.setTableName(tableName);
        }

        if (nodeIdColumn != null) {
            controller.setNodeIdColumn(nodeIdColumn);
        }

        if (queueNameColumn != null) {
            controller.setQueueNameColumn(queueNameColumn);
        }

        return new PersistentQueue(manager, maxSize, controller);
    }

    public Class<?> getObjectType() {
        return PersistentQueue.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
