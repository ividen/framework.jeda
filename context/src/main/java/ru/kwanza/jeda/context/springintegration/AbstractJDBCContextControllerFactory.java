package ru.kwanza.jeda.context.springintegration;

import ru.kwanza.dbtool.core.DBTool;
import ru.kwanza.dbtool.core.VersionGenerator;
import ru.kwanza.jeda.api.IContextController;
import ru.kwanza.jeda.api.IJedaManager;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.SmartFactoryBean;
import ru.kwanza.jeda.api.internal.IJedaManagerInternal;

abstract class AbstractJDBCContextControllerFactory
        implements SmartFactoryBean<IContextController>, BeanNameAware {

    protected IJedaManagerInternal manager;
    protected String name;

    protected DBTool dbTool;
    protected VersionGenerator versionGenerator;
    protected String terminator;
    protected String tableName;
    protected String idColumnName;
    protected String versionColumnName;
    protected String terminatorColumnName;

    public void setBeanName(String name) {
        this.name = name;
    }

    public boolean isPrototype() {
        return false;
    }

    public boolean isEagerInit() {
        return true;
    }

    public void setManager(IJedaManagerInternal manager) {
        this.manager = manager;
    }

    public boolean isSingleton() {
        return true;
    }

    public void setDbTool(DBTool dbTool) {
        this.dbTool = dbTool;
    }

    public void setVersionGenerator(VersionGenerator versionGenerator) {
        this.versionGenerator = versionGenerator;
    }

    public void setTerminator(String terminator) {
        this.terminator = terminator;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setIdColumnName(String idColumnName) {
        this.idColumnName = idColumnName;
    }

    public void setVersionColumnName(String versionColumnName) {
        this.versionColumnName = versionColumnName;
    }

    public void setTerminatorColumnName(String terminatorColumnName) {
        this.terminatorColumnName = terminatorColumnName;
    }

}
