package ru.kwanza.jeda.timer.jdbc;

import ru.kwanza.autokey.api.AutoKeyValueSequence;
import ru.kwanza.autokey.api.IAutoKey;
import ru.kwanza.dbtool.core.DBTool;
import ru.kwanza.dbtool.core.UpdateException;
import ru.kwanza.dbtool.core.UpdateSetter;
import ru.kwanza.dbtool.core.lock.AppLock;
import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.TimerItem;
import ru.kwanza.jeda.clusterservice.old.ClusterService;
import ru.kwanza.jeda.timer.ITimerPersistentController;
import ru.kwanza.jeda.timer.TimerException;
import org.springframework.jdbc.core.RowMapper;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;


public class JDBCTimerPersistentController implements ITimerPersistentController {

    private class EventPersistSetter implements UpdateSetter<TimerItem> {
        private long nodeId;
        private String timerName;

        private EventPersistSetter(long nodeId, String timerName) {
            this.nodeId = nodeId;
            this.timerName = timerName;
        }

        public boolean setValues(PreparedStatement pst, TimerItem object) throws SQLException {
            pst.setString(1, object.getTimerHandle());
            ByteArrayOutputStream baos;
            ObjectOutputStream oos;
            try {
                baos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(baos);
                oos.writeObject(object.getEvent());
                pst.setObject(2, baos.toByteArray());
            } catch (IOException e) {
                throw new SQLException(e);
            }
            pst.setLong(3, object.getMillis());
            pst.setString(4, timerName);
            pst.setLong(5, nodeId);
            return true;
        }
    }

    private static class EventRowMapper implements RowMapper<TimerItem> {
        public TimerItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            String handle = rs.getString(1);
            Long timeout = rs.getLong(3);
            byte[] data = rs.getBytes(2);
            try {
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                IEvent event = (IEvent) ois.readObject();
                return new TimerItem(event, timeout, handle);
            } catch (Exception e) {
                throw new SQLException(e);
            }
        }
    }

    private static class EventRemoveSetter implements UpdateSetter<TimerItem> {
        public boolean setValues(PreparedStatement pst, TimerItem object) throws SQLException {
            pst.setString(1, object.getTimerHandle());
            return true;
        }
    }

    private class EventUpdateSetter implements UpdateSetter<TimerItem> {

        private long nodeId;

        private EventUpdateSetter(long nodeId) {
            this.nodeId = nodeId;
        }

        public boolean setValues(PreparedStatement pst, TimerItem object) throws SQLException {
            pst.setLong(1, nodeId);
            pst.setObject(2, object.getTimerHandle());
            return true;
        }
    }

    public static final String SEQUENCE_NAME = TimerItem.class.getName();

    private static final EventRowMapper ROW_MAPPER = new EventRowMapper();
    private static final EventRemoveSetter REMOVE_SETTER = new EventRemoveSetter();

    private static String INSERT_SQL = "insert into %s (%s, %s, %s, %s, %s) values (?, ?, ?, ?, ?)";
    private static String DELETE_SQL = "delete from %s where %s = ?";
    private static String SELECT_COUNT_ORACLE_SQL =
            "select %s, %s, %s from (select %s, %s, %s from %s where %s = ? and %s = ? and %s >= ? order by TIMEOUT) where rownum <= ?";
    //    private static String SELECT_COUNT_ORACLE_SQL = "select %s, %s, %s from %s where %s = ? and %s = ? and rownum <= ?";
    private static String SELECT_COUNT_MSSQL_SQL =
            "select top %s %s, %s, %s from %s where %s = ? and %s = ? and %s >= ? order by %s";
    //    private static String SELECT_COUNT_MSSQL_SQL = "select top %s %s, %s, %s from %s where %s = ? and %s = ?";
    private static String UPDATE_SQL = "update %s set %s = ? where %s = ?";
    private static String COUNT_SQL = "select count(*) from %s where %s = ? and %s = ?";

    private DBTool dbTool;
    private IAutoKey autoKey;
    private AppLock lock;

    private String tableName = "event_timer";
    private String idColumn = "timer_handle";
    private String eventColumn = "data";
    private String timeoutColumn = "timeout";
    private String nameColumn = "timer_name";
    private String nodeIdColumn = "node_id";

    private String timerName = "default";

    public JDBCTimerPersistentController(String timerName) {
        if (timerName != null) {
            this.timerName = timerName;
        }
    }

    public JDBCTimerPersistentController() {
    }

    public void setDbTool(DBTool dbTool) {
        this.dbTool = dbTool;
    }

    public void setAutoKey(IAutoKey autoKey) {
        this.autoKey = autoKey;
    }

    public void delete(Collection<TimerItem> result) {
        checkLock();
        try {
            dbTool.update(getRemoveSQL(), result, REMOVE_SETTER);
        } catch (UpdateException e) {
            throw new TimerException(e);
        }
    }

    public Collection<TimerItem> load(long size, long fromMillis) {
        checkLock();
        lock.lock();
        try {
            DBTool.DBType dbType = dbTool.getDbType();
            long nodeId = ClusterService.getNodeId();
            if (dbType.equals(DBTool.DBType.ORACLE)) {
                return dbTool.selectList(getSelectCountOracleSQL(), ROW_MAPPER, nodeId, timerName, fromMillis, size);
            } else if (dbType.equals(DBTool.DBType.MSSQL)) {
                return dbTool.selectList(getSelectCountMSSQL(size), ROW_MAPPER, nodeId, timerName, fromMillis);
            } else {
                throw new RuntimeException("Unknown type of database!");
            }
        } finally {
            lock.close();
        }
    }

    public long getSize() {
        checkLock();
        int nodeId = (int) ClusterService.getNodeId();
        try {
            PreparedStatement preparedStatement =
                    dbTool.getDataSource().getConnection().prepareStatement(getCountSQL());
            preparedStatement.setString(1, timerName);
            preparedStatement.setLong(2, nodeId);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException e) {
            throw new TimerException(e);
        }
    }

    public void persist(Collection<TimerItem> events) {
        checkLock();
        long nodeId = ClusterService.getNodeId();
        setTimerHandle(events);
        try {
            dbTool.update(getPersistSQL(), events, new EventPersistSetter(nodeId, timerName));
        } catch (UpdateException e) {
            throw new TimerException(e);
        }
    }

    public Collection<TimerItem> transfer(long count, int oldNodeId) {
        checkLock();
        List<TimerItem> result;
        lock.lock();
        long nodeId = ClusterService.getNodeId();
        try {
            DBTool.DBType dbType = dbTool.getDbType();
            if (dbType.equals(DBTool.DBType.ORACLE)) {
                result = dbTool.selectList(getSelectCountOracleSQL(), ROW_MAPPER, oldNodeId, timerName, 0, count);
            } else if (dbType.equals(DBTool.DBType.MSSQL)) {
                result = dbTool.selectList(getSelectCountMSSQL(count), ROW_MAPPER, oldNodeId, timerName, 0);
            } else {
                throw new RuntimeException("Unknown type of database!");
            }
            dbTool.update(getUpdateSQL(), result, new EventUpdateSetter(nodeId));
            return result;
        } catch (UpdateException e) {
            throw new TimerException(e);
        } finally {
            lock.close();
        }
    }

    private String getPersistSQL() {
        return String.format(INSERT_SQL, tableName, idColumn, eventColumn, timeoutColumn, nameColumn, nodeIdColumn);
    }

    private String getRemoveSQL() {
        return String.format(DELETE_SQL, tableName, idColumn);
    }

    private String getSelectCountOracleSQL() {
        return String.format(SELECT_COUNT_ORACLE_SQL, idColumn, eventColumn, timeoutColumn, idColumn, eventColumn,
                timeoutColumn, tableName, nodeIdColumn, nameColumn, timeoutColumn);
    }

    private String getSelectCountMSSQL(long size) {
        return String
                .format(SELECT_COUNT_MSSQL_SQL, size, idColumn, eventColumn, timeoutColumn, tableName, nodeIdColumn,
                        nameColumn, timeoutColumn, timeoutColumn);
    }

    private String getUpdateSQL() {
        return String.format(UPDATE_SQL, tableName, nodeIdColumn, idColumn);
    }

    private String getCountSQL() {
        return String.format(COUNT_SQL, tableName, nameColumn, nodeIdColumn);
    }

    private void checkLock() {
        if (dbTool == null) {
            throw new RuntimeException("DBTool is not initialized!");
        }
        if (lock == null) {
            lock = dbTool.getLock(tableName + "_" + timerName);
        }
    }

    private void setTimerHandle(Collection<TimerItem> events) {
        if (autoKey == null) {
            throw new RuntimeException("AutoKey is not initialized!");
        }
        AutoKeyValueSequence keys = autoKey.getValueSequence(SEQUENCE_NAME, events.size());
        for (TimerItem evt : events) {
            evt.setTimerHandle(String.valueOf(keys.next()));
        }
    }
}
