package ru.kwanza.jeda.persistentqueue.old.jdbc;

import ru.kwanza.autokey.api.AutoKeyValueSequence;
import ru.kwanza.autokey.api.IAutoKey;
import ru.kwanza.dbtool.core.DBTool;
import ru.kwanza.dbtool.core.UpdateException;
import ru.kwanza.dbtool.core.UpdateSetter;
import ru.kwanza.dbtool.core.lock.AppLock;
import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.persistentqueue.old.EventWithKey;
import ru.kwanza.jeda.persistentqueue.old.IQueuePersistenceController;
import ru.kwanza.jeda.persistentqueue.old.PersistenceQueueException;
import org.springframework.jdbc.core.RowMapper;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * @author Guzanov Alexander
 */
public class JDBCQueuePersistenceController implements IQueuePersistenceController {

    public static final String SEQUENCE_NAME = EventWithKey.class.getName();

    private static String INSERT_SQL = "insert into %s (%s, %s, %s, %s) values (?, ?, ?, ?)";
    private static String SELECT_ALL_SQL = "select %s, %s from %s where %s = ? and %s = ?";
    private static String SELECT_COUNT_ORACLE_SQL = "select %s, %s from %s where %s = ? and %s = ? and rownum <= ?";
    private static String SELECT_COUNT_MSSQL_SQL = "select top %s %s, %s from %s where %s = ? and %s = ?";
    private static String UPDATE_SQL = "update %s set %s = ? where %s = ?";
    private static String DELETE_SQL = "delete from %s where %s = ?";

    private static final EventRowMapper ROW_MAPPER = new EventRowMapper();
    private static final EventRemoveSetter REMOVE_SETTER = new EventRemoveSetter();

    private DBTool dbTool;
    private IAutoKey autoKey;
    private AppLock lock;

    private String tableName = "event_queue";
    private String idColumn = "id";
    private String eventColumn = "data";
    private String nodeIdColumn = "node_id";
    private String queueNameColumn = "queue_name";

    private String queueName = "default";

    public JDBCQueuePersistenceController(String queueName) {
        if (queueName != null) {
            this.queueName = queueName;
        }
    }

    public JDBCQueuePersistenceController() {
        this(null);
    }

    public void setDbTool(DBTool dbTool) {
        this.dbTool = dbTool;
    }

    public void setAutoKey(IAutoKey autoKey) {
        this.autoKey = autoKey;
    }

    public Collection<EventWithKey> load(long nodeId) {
        isNull();
        checkLock();
        lock.lock();
        try {
            return dbTool.selectList(getSelectAllSQL(), ROW_MAPPER, nodeId, queueName);
        } finally {
            lock.close();
        }
    }

    public void persist(Collection<EventWithKey> events, long nodeId) {
        isNull();
        try {
            setIds(events);
            dbTool.update(getPersistSQL(), events, new EventPersistSetter(nodeId, queueName));
        } catch (UpdateException e) {
            throw new PersistenceQueueException(e);
        }
    }

    public Collection<EventWithKey> transfer(int maxSize, long currentNodeId, long newNodeId) {
        isNull();
        checkLock();
        List<EventWithKey> result;
        lock.lock();
        try {
            DBTool.DBType dbType = dbTool.getDbType();
             if (dbType.equals(DBTool.DBType.MSSQL)) {
                result = dbTool.selectList(getSelectCountMSSQL(maxSize), ROW_MAPPER, currentNodeId, queueName);
            } else {
                 result = dbTool.selectList(getSelectCountOracleSQL(), ROW_MAPPER, currentNodeId, queueName, maxSize);
             }
            dbTool.update(getUpdateSQL(), result, new EventUpdateSetter(newNodeId));
            return result;
        } catch (UpdateException e) {
            throw new PersistenceQueueException(e);
        } finally {
            lock.close();
        }
    }

    public void delete(Collection<EventWithKey> result, long nodeId) {
        isNull();
        try {
            dbTool.update(getRemoveSQL(), result, REMOVE_SETTER);
        } catch (UpdateException e) {
            throw new PersistenceQueueException(e);
        }
    }

    private void setIds(Collection<EventWithKey> events) {
        if (autoKey == null) {
            throw new RuntimeException("AutoKey is not initialized!");
        }
        AutoKeyValueSequence keys = autoKey.getValueSequence(SEQUENCE_NAME, events.size());
        for (EventWithKey evt : events) {
            evt.setKey(keys.next());
        }
    }

    private String getSelectAllSQL() {
        return String.format(SELECT_ALL_SQL, idColumn, eventColumn, tableName, nodeIdColumn, queueNameColumn);
    }

    private String getSelectCountOracleSQL() {
        return String.format(SELECT_COUNT_ORACLE_SQL, idColumn, eventColumn, tableName, nodeIdColumn, queueNameColumn);
    }

    private String getSelectCountMSSQL(long size) {
        return String.format(SELECT_COUNT_MSSQL_SQL, size, idColumn, eventColumn, tableName, nodeIdColumn, queueNameColumn);
    }

    private String getPersistSQL() {
        return String.format(INSERT_SQL, tableName, idColumn, eventColumn, nodeIdColumn, queueNameColumn);
    }

    private String getUpdateSQL() {
        return String.format(UPDATE_SQL, tableName, nodeIdColumn, idColumn);
    }

    private String getRemoveSQL() {
        return String.format(DELETE_SQL, tableName, idColumn);
    }

    private void checkLock() {
        if (lock == null) {
            lock = dbTool.getLock(tableName + "_" + queueName);
        }
    }

    private void isNull() {
        if (dbTool == null) {
            throw new RuntimeException("DBTool is not initialized!");
        }
    }

    private static class EventRowMapper implements RowMapper<EventWithKey> {
        public EventWithKey mapRow(ResultSet rs, int rowNum) throws SQLException {
            long id = rs.getLong(1);
            byte[] data = rs.getBytes(2);
            try {
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                IEvent event = (IEvent) ois.readObject();
                return new EventWithKey(id, event);
            } catch (Exception e) {
                throw new SQLException(e);
            }
        }
    }

    private class EventPersistSetter implements UpdateSetter<EventWithKey> {
        private long nodeId;
        private String queueName;

        private EventPersistSetter(long nodeId, String queueName) {
            this.nodeId = nodeId;
            this.queueName = queueName;
        }

        public boolean setValues(PreparedStatement pst, EventWithKey object) throws SQLException {
            pst.setLong(1, (Long) object.getKey());
            ByteArrayOutputStream baos;
            ObjectOutputStream oos;
            try {
                baos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(baos);
                oos.writeObject(object.getDelegate());
                pst.setObject(2, baos.toByteArray());
            } catch (IOException e) {
                throw new SQLException(e);
            }
            pst.setLong(3, nodeId);
            pst.setString(4, queueName);
            return true;
        }
    }

    private class EventUpdateSetter implements UpdateSetter<EventWithKey> {

        private long nodeId;

        private EventUpdateSetter(long nodeId) {
            this.nodeId = nodeId;
        }

        public boolean setValues(PreparedStatement pst, EventWithKey object) throws SQLException {
            pst.setLong(1, nodeId);
            pst.setObject(2, object.getKey());
            return true;
        }
    }

    private static class EventRemoveSetter implements UpdateSetter<EventWithKey> {
        public boolean setValues(PreparedStatement pst, EventWithKey object) throws SQLException {
            pst.setLong(1, (Long) object.getKey());
            return true;
        }
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

    public void setQueueNameColumn(String queueNameColumn) {
        this.queueNameColumn = queueNameColumn;
    }
}