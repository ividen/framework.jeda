package ru.kwanza.jeda.timerservice.pushtimer.dao.basis;

import ru.kwanza.dbtool.core.DBTool;
import ru.kwanza.dbtool.core.FieldGetter;
import ru.kwanza.jeda.timerservice.pushtimer.TimerEntity;
import ru.kwanza.jeda.timerservice.pushtimer.TimerState;
import ru.kwanza.jeda.timerservice.pushtimer.dao.IFetchCursor;
import ru.kwanza.jeda.timerservice.pushtimer.dao.handle.ITimerHandleMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Michael Yeskov
 */
public class OracleFetchCursor implements IFetchCursor {

    private long currentLeftBorder;
    private long currentRightBorder;
    private long bucketId;
    private long fetchSize;

    private TimerMapping mapping;
    private ITimerHandleMapper handleMapper;


    private static final String SQL_TEMPLATE = "SELECT * from (SELECT %s FROM %s WHERE %s = ? AND %s >= ?  AND %s <= ? ORDER BY %s, %s) where ROWNUM < %s";
    private String sql;

    private DBTool dbTool;

    private Connection connection;
    private PreparedStatement pst;
    private ResultSet rs;

    private boolean isOpen = false;


    public OracleFetchCursor(DBTool dbTool, long leftBorder, long rightBorder, long bucketId, TimerMapping mapping, ITimerHandleMapper handleMapper, long fetchSize) {
        currentLeftBorder = leftBorder;
        currentRightBorder = rightBorder;

        this.bucketId = bucketId;
        this.fetchSize = fetchSize;
        this.mapping = mapping;
        this.handleMapper = handleMapper;


        this.dbTool = dbTool;

        sql = String.format(SQL_TEMPLATE,
                mapping.getListOfAll5Fields(),
                mapping.getTableName(),  mapping.getBucketIdField(), mapping.getExpireTimeField(),
                mapping.getExpireTimeField(),  mapping.getBucketIdField(), mapping.getExpireTimeField(), fetchSize + 1);
    }

    @Override
    public void open() {
        if (isOpen) {
            throw new IllegalStateException("Already opened");
        }
        try {
            connection = dbTool.getJDBCConnection();
            pst = connection.prepareStatement(sql);
        } catch (SQLException e) {
            closeCursor();
            throw new RuntimeException(e);
        }
        isOpen = true;
    }

    @Override


    public boolean fetchInto(List<TimerEntity> firedTimers) {
        if (!isOpen) {
            throw new IllegalStateException("Cursor is not opened");
        }

        try {
            pst.setLong(1, bucketId);
            pst.setLong(2, currentLeftBorder);
            pst.setLong(3, currentRightBorder);
            rs = pst.executeQuery();
            rs.setFetchDirection(ResultSet.FETCH_FORWARD);
            rs.setFetchSize((int)fetchSize);

            int justFetchedSize = 0;
            while (justFetchedSize < fetchSize){
                if (rs.next()) {
                    currentLeftBorder  = FieldGetter.getLong(rs, mapping.getExpireTimeField());
                    if (currentLeftBorder > currentRightBorder) { //it is possible due right border adjust with helper
                        closeCursor();
                        return true;
                    }

                    firedTimers.add(
                            new TimerEntity(
                                    handleMapper.fromRs(rs, 1),
                                    TimerState.byId(FieldGetter.getLong(rs, mapping.getStateField())),
                                    FieldGetter.getLong(rs, mapping.getBucketIdField()),
                                    currentLeftBorder,
                                    FieldGetter.getLong(rs, mapping.getCreationPointCountField())
                            ));

                    justFetchedSize++;
                } else {
                    closeCursor();
                    return true;
                }
            }
            dbTool.closeResources(rs);
            return false;
        } catch (Exception e) {
            closeCursor();
            throw new RuntimeException(e);
        }

    }

    @Override
    public void close() {
        closeCursor();
    }

    private void closeCursor() {
        isOpen = false;
        dbTool.closeResources(rs, pst, connection);
    }

    @Override
    public long getCurrentLeftBorder() {
        return currentLeftBorder;
    }

    @Override
    public void setCurrentRightBorder(long rightBorder) {
        this.currentRightBorder = rightBorder;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }
}
