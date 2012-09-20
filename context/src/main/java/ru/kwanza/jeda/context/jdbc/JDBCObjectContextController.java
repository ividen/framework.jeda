package ru.kwanza.jeda.context.jdbc;

import ru.kwanza.dbtool.*;
import ru.kwanza.jeda.api.ContextStoreException;
import ru.kwanza.jeda.context.ObjectContext;
import ru.kwanza.toolbox.fieldhelper.FieldHelper;
import ru.kwanza.txn.api.Transactional;
import ru.kwanza.txn.api.TransactionalType;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import java.beans.PropertyDescriptor;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Dmitry Zagorovsky
 */
public class JDBCObjectContextController<T extends ObjectContext> extends AbstractJDBCContextController<Long, T> {

    private static final Logger log = LoggerFactory.getLogger(JDBCObjectContextController.class);

    private Class<? extends T> clazz;

    private Map<String, String> tableColumnByPropertyName;

    private Map<String, String> propertyByColumn = new TreeMap<String, String>();
    private Map<String, Class> typeByProperty = new HashMap<String, Class>();

    private ContextItemInsertSetter ctxItemInsertSetter = new ContextItemInsertSetter();
    private ContextItemUpdateSetter ctxItemUpdateSetter = new ContextItemUpdateSetter();
    private ContextItemRemoveSetter ctxItemRemoveSetter = new ContextItemRemoveSetter();
    private ContextItemRowMapper ctxItemRowMapper = new ContextItemRowMapper();
    private IdVersionRowMapper ID_VERSION_ROW_MAPPER = new IdVersionRowMapper();

    public final FieldHelper.Field<T, Long> KEY = new FieldHelper.Field<T, Long>() {
        public Long value(T object) {
            return object.getId();
        }
    };

    public JDBCObjectContextController() {
    }

    public JDBCObjectContextController(Class<? extends T> clazz,
                                       Map<String, String> tableColumnByPropertyName) {
        this.clazz = clazz;
        this.tableColumnByPropertyName = tableColumnByPropertyName;
        tableName = "object_context";
        initMapping();
        initSqlBuilder();
    }

    public T createEmptyValue(Long contextId) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional(value = TransactionalType.REQUIRED, applicationExceptions = ContextStoreException.class)
    public Map<Long, T> load(Collection<Long> contextIds) {
        ArrayList<Object> params = new ArrayList<Object>();
        params.add(contextIds);
        if (terminator != null) {
            params.add(terminator);
        }
        return dbTool.selectMap(sqlBuilder.getSelectSql(), ctxItemRowMapper, params.toArray());
    }

    protected void storeNewContextItems(List<T> contextItems) throws ContextStoreException {
        try {
            dbTool.update(sqlBuilder.getInsertSql(), contextItems, ctxItemInsertSetter);
        } catch (UpdateException e) {
            throw new ContextStoreException(e.getOptimistic(), e.getConstrainted());
        }
    }

    protected void updateContextItems(List<T> contextItems) throws ContextStoreException {
        try {
            dbTool.update(sqlBuilder.getUpdateSql(),
                    contextItems, ctxItemUpdateSetter,
                    sqlBuilder.getCheckUpdateSql(), ID_VERSION_ROW_MAPPER, KEY, new ObjectContext.VersionFieldImpl<T>(versionGenerator));
        } catch (UpdateException e) {
            throw new ContextStoreException(e.getOptimistic(), e.getConstrainted());
        }
    }

    protected void blockBeforeRemove(Collection<T> contexts) throws UpdateException {
        dbTool.update(sqlBuilder.getUpdateSql(),
                contexts, ctxItemUpdateSetter,
                sqlBuilder.getCheckUpdateSql(), ID_VERSION_ROW_MAPPER, KEY, new ObjectContext.VersionFieldImpl<T>(versionGenerator));
    }

    protected void removeBlocked(Collection<T> contextToRemoveList) throws UpdateException {
        dbTool.update(sqlBuilder.getDeleteSql(), contextToRemoveList, ctxItemRemoveSetter);
    }

    @Override
    protected void setContextVersion(T context, Long version) {
        context.setVersion(version);
    }

    private class ContextItemInsertSetter implements UpdateSetter<T> {
        public boolean setValues(PreparedStatement pst, T object) throws SQLException {
            FieldSetter.setLong(pst, 1, object.getId());
            FieldSetter.setLong(pst, 2, INITIAL_CTX_VERSION);

            int beanDataStartIdx = 4;
            if (terminator != null) {
                FieldSetter.setString(pst, 3, terminator);
            } else {
                beanDataStartIdx = 3;
            }

            populateStatementWithBeanProperties(object, pst, beanDataStartIdx);
            return true;
        }
    }

    private class ContextItemUpdateSetter implements UpdateSetterWithVersion<T, Long> {
        public boolean setValues(PreparedStatement pst, T object, Long newVersion, Long oldVersion)
                throws SQLException {

            FieldSetter.setLong(pst, 1, newVersion);

            populateStatementWithBeanProperties(object, pst, 2);

            int idx = 1 + propertyByColumn.size();

            FieldSetter.setLong(pst, ++idx, object.getId());
            FieldSetter.setLong(pst, ++idx, oldVersion);

            if (terminator != null) {
                FieldSetter.setString(pst, ++idx, terminator);
            }
            return true;
        }
    }

    private class ContextItemRemoveSetter implements UpdateSetter<T> {
        public boolean setValues(PreparedStatement pst, T object) throws SQLException {
            FieldSetter.setLong(pst, 1, object.getId());
            if (terminator != null) {
                FieldSetter.setString(pst, 2, terminator);
            }
            return true;
        }
    }

    private class IdVersionRowMapper implements RowMapper<KeyValue<Long, Long>> {
        public KeyValue<Long, Long> mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new KeyValue<Long, Long>(rs.getLong(getIdColumnName()), rs.getLong(getVersionColumnName()));
        }
    }

    private void populateStatementWithBeanProperties(T object,
                                                     PreparedStatement pst,
                                                     int beanDataStartIdx) throws SQLException {
        for (Map.Entry<String, String> entry : propertyByColumn.entrySet()) {
            Object property;
            try {
                property = PropertyUtils.getSimpleProperty(object, entry.getValue());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            pst.setObject(beanDataStartIdx++, property);
        }
    }

    public class ContextItemRowMapper implements RowMapper<KeyValue<Long, T>> {
        public KeyValue<Long, T> mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long id = rs.getLong(1);
            Long version = rs.getLong(2);

            T objectContext = createEmptyValue(id);
            objectContext.setVersion(version);

            int beanDataStartIdx = 3;
            if (terminator != null) {
                beanDataStartIdx = 4;
                rs.getString(3); // Игнорируем при вычитывании.
            }

            for (int i = beanDataStartIdx; i <= rs.getMetaData().getColumnCount(); i++) {
                try {
                    String columnName = rs.getMetaData().getColumnName(i).toLowerCase();
                    String propertyName = propertyByColumn.get(columnName);
                    Object value = JdbcUtils.getResultSetValue(rs, i, typeByProperty.get(propertyName));
                    PropertyUtils.setSimpleProperty(objectContext, propertyName, value);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return new KeyValue<Long, T>(id, objectContext);
        }
    }

    protected void initMapping() {
        propertyByColumn.clear();
        typeByProperty.clear();
        PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(clazz);
        for (PropertyDescriptor descriptor : descriptors) {
            // пїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅпїЅ пїЅпїЅ пїЅпїЅпїЅпїЅ-пїЅпїЅ, пїЅпїЅпїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅ пїЅ пїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅ
            if (descriptor.getReadMethod() == null || descriptor.getWriteMethod() == null) {
                continue;
            }

            String propertyName = descriptor.getName();

            if ("version".equals(propertyName) || "id".equals(propertyName)) {
                continue;
            }

            String userSpecifiedColumnName = tableColumnByPropertyName.get(propertyName);

            String columnName;
            if (userSpecifiedColumnName != null) {
                columnName = userSpecifiedColumnName;
            } else {
                columnName = propertyName;
                log.warn("Database column mapping is not specified for property '" + propertyName +
                        "'. Use property name instead.");
            }
            typeByProperty.put(propertyName, descriptor.getPropertyType());
            propertyByColumn.put(columnName.toLowerCase(), propertyName);
        }
    }

    @Override
    protected void initSqlBuilder() {
        sqlBuilder = new JDBCContextSqlBuilder(this, propertyByColumn.keySet());
    }

    Map<String, String> getPropertyByColumn() {
        return propertyByColumn;
    }

}
