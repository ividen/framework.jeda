package ru.kwanza.jeda.timerservice.pushtimer.dao.basis;

import ru.kwanza.dbtool.core.*;
import ru.kwanza.jeda.timerservice.pushtimer.TimerEntity;
import ru.kwanza.jeda.timerservice.pushtimer.TimerState;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

/**
 * @author Michael Yeskov
 */
public class UpdatingDBTimerDAO extends AbstractDBTimerDAO {

    private static final String UPDATE_SQL_TEMPLATE = "UPDATE %s SET %s = ?, %s = ?, %s = ?, %s = %s + 1 WHERE %s = ?";
    private String scheduleSQLCache = null;
    private String reScheduleSQLCache = null;

    @Override
    public void scheduleTimers(Set<TimerEntity> timers, final long bucketId) throws UpdateException {

        if (scheduleSQLCache == null) {
            scheduleSQLCache = baseSQL() + " AND " + mapping.getStateField() + " <> ?";
        }

        long updateCount =  dbTool.update(scheduleSQLCache, timers, new UpdateSetter<TimerEntity>() {

            @Override
            public boolean setValues(PreparedStatement pst, TimerEntity object) throws SQLException {

                pst.setLong(1, bucketId);
                pst.setLong(2, object.getExpireTime());
                pst.setLong(3, TimerState.ACTIVE.getId());
                pst.setObject(4, handleMapper.toId(object), handleMapper.getSQLType());
                pst.setLong(5, TimerState.ACTIVE.getId());
                return true;
            }});

        if (updateCount != timers.size()) {
            throw new RuntimeException("Some timers are in active state or absent"); //we always interrupt timers before creating new one
        }
    }


    private String baseSQL() {
        return  String.format(UPDATE_SQL_TEMPLATE, mapping.getTableName(),
                mapping.getBucketIdField(), mapping.getExpireTimeField(), mapping.getStateField(),
                mapping.getCreationPointCountField(), mapping.getCreationPointCountField(),
                mapping.getIdField());
    }


    @Override
    public void reScheduleTimers(Set<TimerEntity> timers, final long bucketId) throws UpdateException {

        if (reScheduleSQLCache == null) {
            reScheduleSQLCache = baseSQL();
        }

        dbTool.update(reScheduleSQLCache, timers, new UpdateSetter<TimerEntity>() {

            @Override
            public boolean setValues(PreparedStatement pst, TimerEntity object) throws SQLException {

                pst.setLong(1, bucketId);
                pst.setLong(2, object.getExpireTime());
                pst.setLong(3, TimerState.ACTIVE.getId());
                pst.setObject(4, handleMapper.toId(object), handleMapper.getSQLType());
                return true;
            }});

    }



}
