package ru.kwanza.jeda.timerservice.pushtimer.dao.basis;

import org.springframework.jdbc.core.RowMapper;
import ru.kwanza.dbtool.core.UpdateException;
import ru.kwanza.jeda.api.pushtimer.manager.TimerHandle;
import ru.kwanza.jeda.timerservice.pushtimer.TimerEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Michael Yeskov
 */
public class InsertMultiUpdateDBTimerDAO extends UpdatingDBTimerDAO{

    private static final String SELECT_ID_TEMPLATE = "SELECT %s FROM %s WHERE %s in (?)";
    private String selectIdSQLCache = null;


    protected Set<TimerEntity> insertAndGetToUpdate(Set<TimerEntity> timers, long bucketId) throws UpdateException {
        Set<TimerHandle> existing = findExisting(timers);

        HashSet<TimerEntity> toInsert = new HashSet<TimerEntity>();
        HashSet<TimerEntity> toUpdate = new HashSet<TimerEntity>();

        for (TimerEntity current : timers) {
            if (existing.contains(current)) {
                toUpdate.add(current);
            } else {
                toInsert.add(current);
            }
        }

        try {
            if (!toInsert.isEmpty()) {
                insertTimers(toInsert, bucketId);
            }
        } catch (UpdateException e) {
            if (!e.getConstrainted().isEmpty()) {
                toUpdate.addAll((Collection)e.getConstrainted());
            } else {
                throw e;
            }
        }

        return toUpdate;
    }

    protected Set<TimerHandle> findExisting(Set<TimerEntity> timers) {
        if (selectIdSQLCache == null) {
            selectIdSQLCache = String.format(SELECT_ID_TEMPLATE, mapping.getIdField(), mapping.getTableName(), mapping.getIdField());
        }
        return  dbTool.selectSet(selectIdSQLCache, new RowMapper<TimerHandle>() {
            @Override
            public TimerHandle mapRow(ResultSet rs, int rowNum) throws SQLException {
                return handleMapper.fromRs(rs, 1);
            }
        }, getIdInValues((Collection)timers));
    }

    @Override
    public void scheduleTimers(Set<TimerEntity> timers, long bucketId) throws UpdateException {
        Set<TimerEntity> toUpdate = insertAndGetToUpdate(timers, bucketId);
        if (!toUpdate.isEmpty()) {
            super.scheduleTimers(toUpdate, bucketId);
        }
    }


    @Override
    public void reScheduleTimers(Set<TimerEntity> timers, long bucketId) throws UpdateException {
        Set<TimerEntity> toUpdate = insertAndGetToUpdate(timers, bucketId);
        if (!toUpdate.isEmpty()) {
            super.reScheduleTimers(toUpdate, bucketId);
        }
    }
}
