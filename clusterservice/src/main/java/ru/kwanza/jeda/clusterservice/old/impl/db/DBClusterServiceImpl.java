package ru.kwanza.jeda.clusterservice.old.impl.db;

import ru.kwanza.jeda.clusterservice.old.INodeListener;
import ru.kwanza.jeda.clusterservice.old.impl.IClusterServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>
 * Мониторинг состояния узлов через таблицу в&nbsp;БД</p>
 * <p> Создание таблицы для оракла
 * <pre>
 *      CREATE TABLE NODE_STATUS
 *      (NODE_ID NUMBER PRIMARY KEY, TS NUMBER)
 *     </pre>
 * </p>
 * <p>
 * При обновлении состояния узлов первым передается всегда состояние текущего узла.
 * Если не указан nodeId, то по умолчанию  0
 * </p>
 * <p>
 * Старт выполняется после инициализации контекста приложения в Spring-е. К&nbsp;этому моменту слушатели должны уже зарегистрироваться в ClusterService
 * </p>
 * <p>
 * <b>Важно</b><br/>
 * Время на всех узлах должно быть синхронизировано через ntp-сервер. В&nbsp;противном случае не гарантируется корректная работа ClusterService
 *
 * </p>
 *
 * @author Guzanov Alexander
 * @see ru.kwanza.jeda.clusterservice.old.ClusterService
 * @see IClusterServiceImpl
 */
public class DBClusterServiceImpl implements IClusterServiceImpl, ApplicationListener<ContextRefreshedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(IClusterServiceImpl.class);
    public static final String NODE_ID_PROPERTY = "jeda.node.id";
    /**
     * id узла
     */
    private long nodeId;
    private ListenerHolder listener;
    private Map<Long, Long> activityMap;
    private JdbcTemplate jdbcTemplate;
    private final static ReentrantLock lock = new ReentrantLock(true);
    /**
     * Интервал сканирования БД. Время в секундах.
     */
    private long checkPeriod;
    /**
     * Время по истечении которого узел считается умершим. Время в секундах.
     */
    private long lifetime;
    private DBCheckerThread checkerThread = null;

    public DBClusterServiceImpl() {
        final String value = System.getProperty(NODE_ID_PROPERTY);
        if (value == null) {
            logger.error("NodeId not found! Default value = 0");
            nodeId = 0;
            //throw new RuntimeException("Set NodeId via property \"" + NODE_ID_PROPERTY + "\'");
        } else {
            nodeId = Long.valueOf(value);
        }
        logger.info("NodeId {}", nodeId);
        activityMap = new HashMap<Long, Long>();
        checkPeriod = 5;
        lifetime = 20;
        listener = new ListenerHolder();
    }

    private void init() {
        if (checkerThread == null) {
            logger.info("Init");
            checkerThread = new DBCheckerThread();
            checkerThread.start();
        }
    }

    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        lock.lock();
        try {
            init();
        } finally {
            lock.unlock();
        }
    }

    public long getLastNodeActivity(long nodeId) {
        Long value = activityMap.get(nodeId);
        if (value == null) return 0;
        return value;
    }

    public void setNodeId(long nodeId) {
        this.nodeId = nodeId;
    }

    public long getNodeId() {
        return nodeId;
    }

    public void subscribe(INodeListener listener) {
        this.listener.subscribe(listener);
    }

    public void unSubscribe(INodeListener listener) {
        this.listener.unSubscribe(listener);
    }

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void setCheckPeriod(long checkPeriod) {
        this.checkPeriod = checkPeriod;
    }

    public void setLifetime(long lifetime) {
        this.lifetime = lifetime;
    }

    private final class DBCheckerThread extends Thread {

        public void run() {
            logger.info("Node checker started");
            registerNodeInDB();
            while (!interrupted()) {
                final List<NodeStatus> statusList = getNodeStatusList();
                long currentTime = System.currentTimeMillis();
                for (NodeStatus status : statusList) {
                    boolean active = ((currentTime - status.getTs()) < lifetime * 1000L); // True - On | False - Off
                    Long lastTime = activityMap.get(status.getNodeId());
                    boolean lastState = lastTime == null ? !active : ((currentTime - lastTime) < lifetime * 1000L);
                    logger.trace("Node {} currentState: {}, lastState: {}", new Object[]{status.getNodeId(), active, lastState});
                    if (listener != null)
                        if (status.getNodeId() == getNodeId()) {
                            if (active && !lastState)       //On && OFF
                                listener.onCurrentNodeActivate();
                            else if (!active && lastState)  //Off && On
                                listener.onCurrentNodeLost();
                        } else {
                            if (active && !lastState)       //On && OFF
                                listener.onNodeActivate(status.getNodeId(), status.getTs());
                            else if (!active && lastState)  //Off && On
                                listener.onNodeLost(status.getNodeId(), status.getTs());
                        }
                    activityMap.put(status.getNodeId(), status.getTs());
                }
                updateNodeStatus();
                try {
                    Thread.sleep(checkPeriod * 1000L);
                } catch (InterruptedException e) {
                    logger.warn("Interrupted");
                }
            }
        }

        private void updateNodeStatus() {
            logger.trace("Update node timestamp");
            try {
                jdbcTemplate.update("update node_status set ts=? where node_id=?", System.currentTimeMillis(), getNodeId());
            } catch (DataAccessException e) {
                logger.warn("Unable update not status in DB. NodeId: {}", getNodeId());
            }
        }

        private void registerNodeInDB() {
            try {
                final int selfCount = jdbcTemplate.queryForInt("select count(node_id) from node_status where node_id=?", getNodeId());
                if (selfCount == 0) {
                    jdbcTemplate.update("insert into node_status (node_id, ts) values (?,?)", getNodeId(), System.currentTimeMillis());
                }
            } catch (DataAccessException e) {
                listener.onCurrentNodeLost();
                logger.error("DB access error", e);
            }
        }

        private List<NodeStatus> getNodeStatusList() {
            logger.trace("Read timestamps for nodes");
            List<NodeStatus> statusList;
            try {
                statusList = jdbcTemplate.query("select node_id, ts from node_status where node_id<>?", new NodeStatusMapper(), getNodeId());
                statusList.add(new NodeStatus(getNodeId(), System.currentTimeMillis()));
            } catch (DataAccessException e) {
                logger.warn("Unable read timestamps from db", e);
                statusList = new ArrayList<NodeStatus>(activityMap.size());
                for (Long id : activityMap.keySet()) {
                    statusList.add(new NodeStatus(id, 0));
                }
            }
            Collections.sort(statusList, new StatusComparator());
            return statusList;
        }
    }

    private class StatusComparator implements Comparator<NodeStatus> {
        public int compare(NodeStatus o1, NodeStatus o2) {
            if (o1.getNodeId() == getNodeId()) return -1;
            if (o2.getNodeId() == getNodeId()) return 1;
            return o1.getNodeId().compareTo(o2.getNodeId());
        }
    }

    private class NodeStatusMapper implements RowMapper<NodeStatus> {
        public NodeStatus mapRow(ResultSet resultSet, int i) throws SQLException {
            long nodeId = resultSet.getLong("node_id");
            Long ts = resultSet.getLong("ts");
            if (resultSet.wasNull()) ts = 0L;
            return new NodeStatus(nodeId, ts);
        }
    }

    private class NodeStatus implements Serializable {
        private Long nodeId;
        private Long ts;

        private NodeStatus(long nodeId, long ts) {
            this.nodeId = nodeId;
            this.ts = ts;
        }

        public Long getNodeId() {
            return nodeId;
        }

        public Long getTs() {
            return ts;
        }
    }

    private class ListenerHolder implements INodeListener {
        private final Set<INodeListener> listenerSet = new HashSet<INodeListener>();
        private final ReentrantLock lock = new ReentrantLock();

        public void onNodeLost(Long nodeId, long lastNodeTs) {
            lock.lock();
            try {
                for (INodeListener listener : listenerSet) {
                    try {
                        listener.onNodeLost(nodeId, lastNodeTs);
                    } catch (Exception e) {
                        logger.warn("Node listener call failed", e);
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        public void onNodeActivate(Long nodeId, long lastNodeTs) {
            lock.lock();
            try {
                for (INodeListener listener : listenerSet) {
                    try {
                        listener.onNodeActivate(nodeId, lastNodeTs);
                    } catch (Exception e) {
                        logger.warn("Node listener call failed", e);
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        public void onCurrentNodeActivate() {
            lock.lock();
            try {
                for (INodeListener listener : listenerSet) {
                    try {
                        listener.onCurrentNodeActivate();
                    } catch (Exception e) {
                        logger.warn("Node listener call failed", e);
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        public void onCurrentNodeLost() {
            lock.lock();
            try {
                for (INodeListener listener : listenerSet) {
                    try {
                        listener.onCurrentNodeLost();
                    } catch (Exception e) {
                        logger.warn("Node listener call failed", e);
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        public void subscribe(INodeListener listener) {
            lock.lock();
            try {
                listenerSet.add(listener);
            } finally {
                lock.unlock();
            }
        }

        public void unSubscribe(INodeListener listener) {
            lock.lock();
            try {
                listenerSet.remove(listener);
            } finally {
                lock.unlock();
            }
        }
    }
}
