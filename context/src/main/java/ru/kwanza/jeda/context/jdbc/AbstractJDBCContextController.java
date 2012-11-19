package ru.kwanza.jeda.context.jdbc;

import ru.kwanza.dbtool.core.DBTool;
import ru.kwanza.dbtool.core.UpdateException;
import ru.kwanza.dbtool.core.VersionGenerator;
import ru.kwanza.jeda.api.ContextStoreException;
import ru.kwanza.jeda.api.IContext;
import ru.kwanza.jeda.api.IContextController;
import ru.kwanza.txn.api.Transactional;
import ru.kwanza.txn.api.TransactionalType;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractJDBCContextController<ID, C extends IContext<ID, ?>>
        implements IContextController<ID, C> {

    protected static final long INITIAL_CTX_VERSION = 1l;

    protected DBTool dbTool;
    protected VersionGenerator versionGenerator;
    protected JDBCContextSqlBuilder sqlBuilder;

    protected String terminator = null;

    protected String tableName = "blob_context";
    protected String idColumnName = "id";
    protected String versionColumnName = "version";
    protected String terminatorColumnName = "terminator";

    public void reInit() {
        initSqlBuilder();
    }

    @Transactional(value = TransactionalType.REQUIRED, applicationExceptions = ContextStoreException.class)
    public void store(Collection<C> contextObjects) throws ContextStoreException {
        List<C> insertContextItems = new LinkedList<C>();
        List<C> updateContextItems = new LinkedList<C>();
        for (C context : contextObjects) {
            if (context.getVersion() == null) {
                setContextVersion(context, INITIAL_CTX_VERSION);
                insertContextItems.add(context);
            } else {
                updateContextItems.add(context);
            }
        }

        if (!insertContextItems.isEmpty()) {
            storeNewContextItems(insertContextItems);
        }

        if (!updateContextItems.isEmpty()) {
            updateContextItems(updateContextItems);
        }
    }

    protected abstract void setContextVersion(C context, Long version);

    protected abstract void storeNewContextItems(List<C> contextObjects) throws ContextStoreException;

    protected abstract void updateContextItems(List<C> contextObjects) throws ContextStoreException;

    @Transactional(value = TransactionalType.REQUIRED, applicationExceptions = ContextStoreException.class)
    public void remove(Collection<C> contexts) throws ContextStoreException {
        //Элементы, которые не удалось захватить до удаленния
        List<C> optimisticItems = null;

        try {
            blockBeforeRemove(contexts);
        } catch (UpdateException e) {
            optimisticItems = initIfAbsent(optimisticItems);
            optimisticItems.addAll(e.<C>getOptimistic());
        }

        //Формируем список элементов для удаления (заданные - незахваченные)
        Collection<C> contextToRemoveList;
        if (optimisticItems == null) {
            contextToRemoveList = contexts;
        } else {
            contextToRemoveList = new LinkedList<C>(contexts);
            contextToRemoveList.removeAll(optimisticItems);
        }

        List<C> otherFailedItems = null;
        try {
            removeBlocked(contextToRemoveList);
        } catch (UpdateException e) {
            //В исключение попадают все незахваченные и все из UpdateException
            optimisticItems = initIfAbsent(optimisticItems);
            optimisticItems.addAll(e.<C>getOptimistic());

            otherFailedItems = initIfAbsent(otherFailedItems);
            otherFailedItems.addAll(e.<C>getConstrainted());
        }

        if (optimisticItems != null) {
            throw new ContextStoreException(optimisticItems, otherFailedItems);
        }
    }

    protected abstract void blockBeforeRemove(Collection<C> contexts) throws UpdateException;

    protected abstract void removeBlocked(Collection<C> contextToRemoveList) throws UpdateException;

    protected abstract void initSqlBuilder();

    protected List<C> initIfAbsent(List<C> list) {
        if (list == null) {
            return new LinkedList<C>();
        } else {
            return list;
        }
    }

    public String getTerminator() {
        return terminator;
    }

    public void setTerminator(String terminator) {
        this.terminator = terminator;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getIdColumnName() {
        return idColumnName;
    }

    public void setIdColumnName(String idColumnName) {
        this.idColumnName = idColumnName;
    }

    public String getVersionColumnName() {
        return versionColumnName;
    }

    public void setVersionColumnName(String versionColumnName) {
        this.versionColumnName = versionColumnName;
    }

    public String getTerminatorColumnName() {
        return terminatorColumnName;
    }

    public void setTerminatorColumnName(String terminatorColumnName) {
        this.terminatorColumnName = terminatorColumnName;
    }

    public void setDbTool(DBTool dbTool) {
        this.dbTool = dbTool;
    }

    public void setVersionGenerator(VersionGenerator versionGenerator) {
        this.versionGenerator = versionGenerator;
    }

}
