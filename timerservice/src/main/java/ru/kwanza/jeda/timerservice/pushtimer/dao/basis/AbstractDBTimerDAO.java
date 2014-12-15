package ru.kwanza.jeda.timerservice.pushtimer.dao.basis;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowMapper;
import ru.kwanza.dbtool.core.*;
import ru.kwanza.jeda.api.timerservice.pushtimer.manager.TimerHandle;
import ru.kwanza.jeda.timerservice.pushtimer.TimerEntity;
import ru.kwanza.jeda.timerservice.pushtimer.TimerState;
import ru.kwanza.jeda.timerservice.pushtimer.dao.IDBTimerDAO;
import ru.kwanza.jeda.timerservice.pushtimer.dao.IFetchCursor;
import ru.kwanza.jeda.timerservice.pushtimer.dao.handle.ITimerHandleMapper;

import javax.annotation.Resource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Michael Yeskov
 */
public abstract class AbstractDBTimerDAO implements IDBTimerDAO {

    protected TimerMapping mapping;
    protected ITimerHandleMapper handleMapper;
    protected boolean useOracleOptimizedFetchCursor = true;
    protected long fetchSize = 1000;


    @Resource(name = "dbtool.DBTool")
    protected DBTool dbTool;


    //other fields
    private static final String INTERRUPT_SQL_TEMPLATE = "UPDATE %s SET %s = ? WHERE %s = ? AND %s = ?";
    private String interruptSQLCache = null;

    private static final String IS_ACTIVE_SQL_TEMPLATE = "SELECT %s, %s FROM %s WHERE %s in (?)";
    private String isActiveSQLCache = null;

    private static final String LOAD_ENTITIES_SQL_TEMPLATE = "SELECT %s FROM %s WHERE %s in (?)";
    private String loadEntitiesSQLCache = null;


    private static final String MARK_FIRED_SQL_TEMPLATE = "UPDATE %s SET %s = ?, %s = ? , %s = ? WHERE %s = ? AND  %s = ? AND %s = ?";
    private String markFiredSQLCache = null;


    private static final String INSERT_SQL_TEMPLATE = "INSERT INTO %s (%s, %s, %s, %s, %s) values (?,?,?,?,?)";
    private String insertSQLCache = null;


    protected void insertTimers(Set<TimerEntity> timers, final long bucketId) throws UpdateException {
        if (insertSQLCache == null) {
            insertSQLCache = String.format(INSERT_SQL_TEMPLATE, mapping.getTableName(),
                    mapping.getIdField(), mapping.getStateField(), mapping.getBucketIdField(),
                    mapping.getExpireTimeField(), mapping.getCreationPointCountField());
        }
        dbTool.update(insertSQLCache, timers,new UpdateSetter<TimerEntity>() {
            @Override
            public boolean setValues(PreparedStatement pst, TimerEntity object) throws SQLException {
                pst.setObject(1, handleMapper.toId(object), handleMapper.getSQLType());
                pst.setLong(2, TimerState.ACTIVE.getId());
                pst.setLong(3, bucketId);
                pst.setLong(4, object.getExpireTime());
                pst.setLong(5, 1);
                return true;
            }
        });
    }


    //methods
    /*
     * Update вариант
     */
    @Override
    public void interruptTimers(Set<TimerHandle> timersToInterrupt) throws UpdateException {
        if (interruptSQLCache == null) {
            interruptSQLCache = String.format(INTERRUPT_SQL_TEMPLATE, mapping.getTableName(), mapping.getStateField(), mapping.getStateField(), mapping.getIdField());
        }
        dbTool.update(interruptSQLCache, timersToInterrupt, new UpdateSetter<TimerHandle>() {
            @Override
            public boolean setValues(PreparedStatement pst, TimerHandle object) throws SQLException {
                pst.setLong(1, TimerState.INTERRUPTED.getId());
                pst.setLong(2, TimerState.ACTIVE.getId());
                pst.setObject(3, handleMapper.toId(object), handleMapper.getSQLType());
                return true;
            }
        });
    }

    protected List<Object> getIdInValues(Collection<TimerHandle> timersToFind){
        List<Object> inValues = new ArrayList<Object>(timersToFind.size());
        for (TimerHandle current : timersToFind) {
            inValues.add(handleMapper.toId(current));
        }
        return inValues;
    }

    @Override
    public Map<TimerHandle, Boolean> getIsActiveMap(Collection<TimerHandle> timersToFind) {
        if (isActiveSQLCache == null) {
            isActiveSQLCache = String.format(IS_ACTIVE_SQL_TEMPLATE, mapping.getIdField(), mapping.getStateField(), mapping.getTableName(), mapping.getIdField());
        }

        List<Object> inValues = getIdInValues(timersToFind);

        Map<TimerHandle, Boolean> result = dbTool.selectMap(isActiveSQLCache, new RowMapper<KeyValue<TimerHandle, Boolean>>() {
            @Override
            public KeyValue<TimerHandle, Boolean> mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new KeyValue<TimerHandle, Boolean>(
                        handleMapper.fromRs(rs, 1),
                        FieldGetter.getLong(rs, mapping.getStateField()) == TimerState.ACTIVE.getId());
            }


        } , inValues);

        for (TimerHandle timerHandle : timersToFind) {
            if (!result.containsKey(timerHandle)) {
                result.put(timerHandle, false);
            }
        }

        return result;
    }


    @Override
    public IFetchCursor createFetchCursor(long leftBorder, long rightBorder, long bucketId) {
        if (dbTool.getDbType().equals(DBTool.DBType.ORACLE)) {
            if (useOracleOptimizedFetchCursor) {
                return new OracleOptimizedFetchCursor(dbTool, leftBorder, rightBorder, bucketId,  mapping, handleMapper, fetchSize);
            } else {
                return new OracleFetchCursor(dbTool, leftBorder, rightBorder, bucketId,  mapping, handleMapper, fetchSize);
            }
        } else {
            throw new RuntimeException("Not implemented yet");
        }
    }


    @Override
    public List<TimerEntity> loadTimerEntities(String timerName, Set<String> timerIds) {
        if (loadEntitiesSQLCache == null) {
            loadEntitiesSQLCache = String.format(LOAD_ENTITIES_SQL_TEMPLATE,
                    mapping.getListOfAll5Fields(), mapping.getTableName(), mapping.getIdField());
        }
        List<Object> inValues = new ArrayList<Object>(timerIds.size());
        for (String id : timerIds) {
            inValues.add(handleMapper.toId(new TimerHandle(timerName, id)));
        }

        return dbTool.selectList(loadEntitiesSQLCache, new RowMapper<TimerEntity>() {
            @Override
            public TimerEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new TimerEntity(
                        handleMapper.fromRs(rs, 1),
                        TimerState.byId(FieldGetter.getLong(rs, mapping.getStateField())),
                        FieldGetter.getLong(rs, mapping.getBucketIdField()),
                        FieldGetter.getLong(rs, mapping.getExpireTimeField()),
                        FieldGetter.getLong(rs, mapping.getCreationPointCountField())
                );
            }
        }, inValues);
    }

    @Override
    public void markFiredWithOptLock(String timerName, final List<TimerEntity> entities) throws UpdateException {
        if (markFiredSQLCache == null) {
            markFiredSQLCache = String.format(MARK_FIRED_SQL_TEMPLATE,
                    mapping.getTableName(),
                    mapping.getStateField(),
                    mapping.getBucketIdField(),
                    mapping.getExpireTimeField(),
                    mapping.getIdField(),
                    mapping.getStateField(),
                    mapping.getCreationPointCountField()
            );
        }

        dbTool.update(markFiredSQLCache, entities, new UpdateSetter<TimerEntity>() {
            @Override
            public boolean setValues(PreparedStatement pst, TimerEntity entity) throws SQLException {
                FieldSetter.setLong(pst, 1, TimerState.FIRED.getId());
                FieldSetter.setLong(pst, 2, null);
                FieldSetter.setLong(pst, 3, null);
                pst.setObject(4, handleMapper.toId(entity), handleMapper.getSQLType());
                FieldSetter.setLong(pst, 5, TimerState.ACTIVE.getId());
                FieldSetter.setLong(pst, 6, entity.getCreationPointCount());
                return true;
            }
        });
    }

    @Override
    public Set<String> getCompatibleTimerNames() {
        return handleMapper.getCompatibleTimerNames();
    }

    @Override
    public long getFetchSize() {
        return fetchSize;
    }

    public void setFetchSize(long fetchSize) {
        this.fetchSize = fetchSize;
    }

    public boolean isUseOracleOptimizedFetchCursor() {
        return useOracleOptimizedFetchCursor;
    }

    public void setUseOracleOptimizedFetchCursor(boolean useOracleOptimizedFetchCursor) {
        this.useOracleOptimizedFetchCursor = useOracleOptimizedFetchCursor;
    }

    public TimerMapping getMapping() {
        return mapping;
    }
    @Required
    public void setMapping(TimerMapping mapping) {
        this.mapping = mapping;
    }

    public ITimerHandleMapper getHandleMapper() {
        return handleMapper;
    }
    @Required
    public void setHandleMapper(ITimerHandleMapper handleMapper) {
        this.handleMapper = handleMapper;
    }

}
