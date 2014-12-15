package ru.kwanza.jeda.timerservice.pushtimer.dao.basis;

import ru.kwanza.dbtool.core.FieldSetter;
import ru.kwanza.dbtool.core.UpdateException;
import ru.kwanza.dbtool.core.UpdateSetter;
import ru.kwanza.jeda.api.timerservice.pushtimer.manager.TimerHandle;
import ru.kwanza.jeda.timerservice.pushtimer.TimerEntity;
import ru.kwanza.jeda.timerservice.pushtimer.TimerState;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * @author Michael Yeskov
 */
public class InsertDeleteDBTimerDAO extends AbstractDBTimerDAO{

    private static final String DELETE_SQL_TEMPLATE = "DELETE FROM %s WHERE %s = ?";
    private String  deleteSqlCache = null;

    private static final String DELETE_FIRED_SQL_TEMPLATE = "DELETE FROM %s WHERE %s = ? AND  %s = ? AND %s = ?";
    private String  deleteFiredSqlCache = null;



    @Override
    public void interruptTimers(Set<TimerHandle> timersToInterrupt) throws UpdateException {
        if (deleteSqlCache == null) {
            deleteSqlCache = String.format(DELETE_SQL_TEMPLATE, mapping.getTableName(), mapping.getIdField());
        }
        dbTool.update(deleteSqlCache, timersToInterrupt, new UpdateSetter<TimerHandle>() {
            @Override
            public boolean setValues(PreparedStatement pst, TimerHandle object) throws SQLException {
                pst.setObject(1, handleMapper.toId(object), handleMapper.getSQLType());
                return true;
            }
        });
    }

    @Override
    public void markFiredWithOptLock(String timerName, List<TimerEntity> entities) throws UpdateException {
        if (deleteFiredSqlCache == null) {
            deleteFiredSqlCache = String.format(DELETE_FIRED_SQL_TEMPLATE, mapping.getTableName(), mapping.getIdField(), mapping.getStateField(), mapping.getCreationPointCountField());
        }
        dbTool.update(deleteFiredSqlCache, entities, new UpdateSetter<TimerEntity>() {
            @Override
            public boolean setValues(PreparedStatement pst, TimerEntity entity) throws SQLException {
                pst.setObject(1, handleMapper.toId(entity), handleMapper.getSQLType());
                FieldSetter.setLong(pst, 2, TimerState.ACTIVE.getId());
                FieldSetter.setLong(pst, 3, entity.getCreationPointCount());
                return true;
            }
        });
    }

    @Override
    public void scheduleTimers(Set<TimerEntity> timers, long bucketId) throws UpdateException {
        insertTimers(timers, bucketId); //throws Exception on duplicate key
    }

    @Override
    public void reScheduleTimers(Set<TimerEntity> timers, long bucketId) throws UpdateException {
        interruptTimers((Set)timers);
        insertTimers(timers, bucketId);// may throw Update exception in case of Optimistic Lock
    }
}
