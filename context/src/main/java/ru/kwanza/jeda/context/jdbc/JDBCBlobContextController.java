package ru.kwanza.jeda.context.jdbc;

import ru.kwanza.dbtool.core.*;
import ru.kwanza.jeda.api.ContextStoreException;
import ru.kwanza.jeda.context.MapContextImpl;
import ru.kwanza.txn.api.Transactional;
import ru.kwanza.txn.api.TransactionalType;
import org.springframework.jdbc.core.RowMapper;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static ru.kwanza.jeda.context.MapContextImpl.KEY;

public class JDBCBlobContextController extends AbstractJDBCContextController<String, MapContextImpl> {

    private static final IdVersionRowMapper ID_VERSION_ROW_MAPPER = new IdVersionRowMapper();

    private String contextDataColumnName = "blob";

    private ContextItemInsertSetter ctxItemInsertSetter = new ContextItemInsertSetter();
    private ContextItemRemoveSetter ctxItemRemoveSetter = new ContextItemRemoveSetter();
    private ContextItemUpdateSetter ctxItemUpdateSetter = new ContextItemUpdateSetter();
    private ContextItemRowMapper ctxItemRowMapper = new ContextItemRowMapper();

    public JDBCBlobContextController() {
        tableName = "blob_context";
        initSqlBuilder();
    }

    public MapContextImpl createEmptyValue(String contextId) {
        return new MapContextImpl(contextId, terminator, null);
    }

    @Transactional(value = TransactionalType.REQUIRED, applicationExceptions = ContextStoreException.class)
    public Map<String, MapContextImpl> load(Collection<String> contextIds) {
        ArrayList<Object> params = new ArrayList<Object>();
        params.add(contextIds);
        if (terminator != null) {
            params.add(terminator);
        }
        return dbTool.selectMap(sqlBuilder.getSelectSql(), ctxItemRowMapper, params.toArray());
    }

    @Override
    protected void blockBeforeRemove(Collection<MapContextImpl> contexts) throws UpdateException {
        dbTool.update(sqlBuilder.getUpdateSql(),
                contexts, ctxItemUpdateSetter,
                sqlBuilder.getCheckUpdateSql(), ID_VERSION_ROW_MAPPER, KEY, new MapContextImpl.VersionFieldImpl(versionGenerator));
    }

    @Override
    protected void removeBlocked(Collection<MapContextImpl> contextToRemoveList) throws UpdateException {
        dbTool.update(sqlBuilder.getDeleteSql(), contextToRemoveList, ctxItemRemoveSetter);
    }


    @Override
    protected void setContextVersion(MapContextImpl context, Long version) {
        context.setVersion(version);
    }

    protected void storeNewContextItems(List<MapContextImpl> contextItems) throws ContextStoreException {
        try {
            dbTool.update(sqlBuilder.getInsertSql(), contextItems, ctxItemInsertSetter);
        } catch (UpdateException e) {
            throw new ContextStoreException(e.getOptimistic(), e.getConstrainted());
        }
    }

    protected void updateContextItems(List<MapContextImpl> contextItems) throws ContextStoreException {
        try {
            dbTool.update(sqlBuilder.getUpdateSql(),
                    contextItems, ctxItemUpdateSetter,
                    sqlBuilder.getCheckUpdateSql(), ID_VERSION_ROW_MAPPER, KEY, new MapContextImpl.VersionFieldImpl(versionGenerator));
        } catch (UpdateException e) {
            throw new ContextStoreException(e.getOptimistic(), e.getConstrainted());
        }
    }

    protected void initSqlBuilder() {
        sqlBuilder = new JDBCContextSqlBuilder(this);
    }

    private class ContextItemInsertSetter implements UpdateSetter<MapContextImpl> {
        public boolean setValues(PreparedStatement pst, MapContextImpl object) throws SQLException {
            FieldSetter.setString(pst, 1, object.getId());
            FieldSetter.setLong(pst, 2, INITIAL_CTX_VERSION);

            int blobColumnIdx = 4;
            if (terminator != null) {
                FieldSetter.setString(pst, 3, terminator);
            } else {
                blobColumnIdx = 3;
            }

            try {
                pst.setObject(blobColumnIdx, serializeContextMap(object.getInnerMap()));
            } catch (IOException e) {
                throw new SQLException(e);
            }

            return true;
        }
    }

    private class ContextItemUpdateSetter implements UpdateSetterWithVersion<MapContextImpl, Long> {
        public boolean setValues(PreparedStatement pst, MapContextImpl object, Long newVersion, Long oldVersion)
                throws SQLException {

            FieldSetter.setLong(pst, 1, newVersion);

            try {
                pst.setObject(2, serializeContextMap(object.getInnerMap()));
            } catch (IOException e) {
                throw new SQLException(e);
            }

            FieldSetter.setString(pst, 3, object.getId());
            FieldSetter.setLong(pst, 4, oldVersion);

            if (terminator != null) {
                FieldSetter.setString(pst, 5, terminator);
            }
            return true;
        }
    }

    private class ContextItemRemoveSetter implements UpdateSetter<MapContextImpl> {
        public boolean setValues(PreparedStatement pst, MapContextImpl object) throws SQLException {
            FieldSetter.setString(pst, 1, object.getId());
            if (terminator != null) {
                FieldSetter.setString(pst, 2, terminator);
            }
            return true;
        }
    }

    public class ContextItemRowMapper implements RowMapper<KeyValue<String, MapContextImpl>> {
        public KeyValue<String, MapContextImpl> mapRow(ResultSet rs, int rowNum) throws SQLException {
            String id = rs.getString(1);
            Long version = rs.getLong(2);

            int blobColumnIdx = 4;
            String term = null;
            if (terminator != null) {
                term = rs.getString(3);
            } else {
                blobColumnIdx = 3;
            }

            byte[] data = rs.getBytes(blobColumnIdx);

            try {
                MapContextImpl context = new MapContextImpl(id, term, version, deserializeContextMap(data));
                return new KeyValue<String, MapContextImpl>(id, context);
            } catch (Exception e) {
                throw new SQLException(e);
            }
        }
    }

    protected byte[] serializeContextMap(Map<String, Object> ctxMap) throws IOException {
        return serializeObject(ctxMap);
    }

    protected Map<String, Object> deserializeContextMap(byte[] data) throws IOException, ClassNotFoundException {
        //noinspection unchecked
        return (Map<String, Object>) deserializeObject(data);
    }

    protected byte[] serializeObject(Object obj) throws IOException {
        ByteArrayOutputStream baos;
        ObjectOutputStream oos;
        baos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        return baos.toByteArray();
    }

    protected Object deserializeObject(byte[] data) throws ClassNotFoundException, IOException {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        return ois.readObject();
    }


    private static final class IdVersionRowMapper implements RowMapper<KeyValue<String, Long>> {
        public KeyValue<String, Long> mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new KeyValue<String, Long>(rs.getString("id"), rs.getLong("version")); //TODO убрать хардкод
        }
    }

    public String getContextDataColumnName() {
        return contextDataColumnName;
    }

    public void setContextDataColumnName(String contextDataColumnName) {
        this.contextDataColumnName = contextDataColumnName;
        initSqlBuilder();
    }

}