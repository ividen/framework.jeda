package ru.kwanza.jeda.timerservice.pushtimer.consuming.supervisor;

import org.springframework.stereotype.Repository;
import ru.kwanza.dbtool.core.DBTool;
import ru.kwanza.dbtool.core.FieldSetter;
import ru.kwanza.jeda.timerservice.pushtimer.consuming.NodeId;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Michael Yeskov
 */
@Repository
public class ConsumerSupervisorDAO {

    @Resource(name = "dbtool.DBTool")
    private DBTool dbTool;

    public Long select(String className, NodeId nodeId) {
        Connection connection = dbTool.getJDBCConnection();
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = connection.prepareStatement("SELECT failover_left_border FROM JEDA_TIMERS_FAILOVER WHERE timer_class = ? AND current_node_id = ? and working_as_node_id = ?");

            FieldSetter.setString(pst, 1, className);
            FieldSetter.setInt(pst, 2, nodeId.getCurrentNodeId());
            FieldSetter.setInt(pst, 3, nodeId.getEffectiveNodeId());
            rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbTool.closeResources(connection, pst, rs);
        }
    }

    public void insert(String className, NodeId nodeId, Long failoverLeftBorder) {
        Connection connection = dbTool.getJDBCConnection();
        PreparedStatement pst = null;
        try {
            pst = connection.prepareStatement("INSERT INTO JEDA_TIMERS_FAILOVER(timer_class, current_node_id, working_as_node_id, failover_left_border) VALUES (?, ?, ?, ?)");

            FieldSetter.setString(pst, 1, className);
            FieldSetter.setInt(pst, 2, nodeId.getCurrentNodeId());
            FieldSetter.setInt(pst, 3, nodeId.getEffectiveNodeId());
            FieldSetter.setLong(pst, 4, failoverLeftBorder);

            pst.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbTool.closeResources(connection, pst);
        }

    }

    public Long selectMaxFailoverLeftBorder(String className, Integer effectiveNodeId) {
        Connection connection = dbTool.getJDBCConnection();
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = connection.prepareStatement("SELECT MAX(failover_left_border) FROM JEDA_TIMERS_FAILOVER WHERE timer_class = ? AND working_as_node_id = ? ");

            FieldSetter.setString(pst, 1, className);
            FieldSetter.setInt(pst, 2, effectiveNodeId);
            rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbTool.closeResources(connection, pst, rs);
        }
    }

    public void update(String className, NodeId nodeId, Long failoverLeftBorder) {
        Connection connection = dbTool.getJDBCConnection();
        PreparedStatement pst = null;
        try {
            pst = connection.prepareStatement("UPDATE JEDA_TIMERS_FAILOVER SET failover_left_border = ? WHERE timer_class = ? AND current_node_id = ? and working_as_node_id  = ?");

            FieldSetter.setLong(pst, 1, failoverLeftBorder);
            FieldSetter.setString(pst, 2, className);
            FieldSetter.setInt(pst, 3, nodeId.getCurrentNodeId());
            FieldSetter.setInt(pst, 4, nodeId.getEffectiveNodeId());
            pst.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbTool.closeResources(connection, pst);
        }
    }
}
