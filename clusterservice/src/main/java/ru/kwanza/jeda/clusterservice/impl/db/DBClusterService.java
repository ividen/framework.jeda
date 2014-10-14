package ru.kwanza.jeda.clusterservice.impl.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kwanza.dbtool.core.UpdateException;
import ru.kwanza.dbtool.orm.api.IEntityManager;
import ru.kwanza.dbtool.orm.api.IQuery;
import ru.kwanza.dbtool.orm.api.If;
import ru.kwanza.dbtool.orm.api.LockType;
import ru.kwanza.jeda.clusterservice.IClusterService;
import ru.kwanza.jeda.clusterservice.IClusteredModule;
import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.jeda.clusterservice.impl.db.orm.ModuleEntity;
import ru.kwanza.jeda.clusterservice.impl.db.orm.NodeEntity;
import ru.kwanza.txn.api.spi.ITransactionManager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

/**
 * @author Alexander Guzanov
 */
public class DBClusterService implements IClusterService {
    public static final String NODE_ID_PROPERTY_NAME = "clusterservice.nodeId";
    public static final String ACTIVITY_SUPERVISOR_NAME = "DBClusterService-ActivitySupervisor";
    public static final String REPAIR_SUPERVISOR_NAME = "DBClusterService-RepairSupervisor";

    @Resource(name = "dbtool.IEntityManager")
    private IEntityManager em;
    @Resource(name = "txn.ITransactionManager")
    private ITransactionManager tm;

    private IQuery<NodeEntity> queryActive;
    private IQuery<NodeEntity> queryPassive;
    private IQuery<NodeEntity> queryAll;
    private IQuery<NodeEntity> queryNode;
    private IQuery<ModuleEntity> queryModules;

    private NodeEntity currentNode;

    private static Logger logger = LoggerFactory.getLogger(DBClusterService.class);

    private long failoverTimeout = 15 * 60 * 1000;
    private long lockTimeout = 60 * 1000;
    private int repairThreadCount = 4;

    private ExecutorService repairExecutor;
    private ActivitySupervisor activitySupervisor;
    private RepairSupervisor repairSupervisor;
    private ConcurrentMap<String, IClusteredModule> modules = new ConcurrentHashMap<String, IClusteredModule>();

    @PostConstruct
    public void init() {
        initQuery();
        initCurrentNode();
        initSupervisors();
    }

    @PreDestroy
    public void destroy() {
        logger.info("Stopping {} ...", ACTIVITY_SUPERVISOR_NAME);
        activitySupervisor.interrupt();
        logger.info("Stopping {} ...", REPAIR_SUPERVISOR_NAME);
        repairSupervisor.interrupt();
    }

    private void initQuery() {
        queryActive = em.queryBuilder(NodeEntity.class).where(If.isGreater("lastActivity")).create();
        queryPassive = em.queryBuilder(NodeEntity.class).where(If.isLessOrEqual("lastActivity")).create();
        queryAll = em.queryBuilder(NodeEntity.class).create();
        queryModules = em.queryBuilder(ModuleEntity.class).where(If.and(If.isEqual("nodeId", "nodeId"), If.in("name", "name"))).create();
    }

    private void initCurrentNode() {
        currentNode = new NodeEntity(Integer.valueOf(System.getProperty(NODE_ID_PROPERTY_NAME)), System.currentTimeMillis());

        if (em.readByKey(NodeEntity.class, currentNode.getId()) == null) {
            try {
                em.create(currentNode);
            } catch (UpdateException e) {
                if (em.readByKey(NodeEntity.class, currentNode.getId()) == null) {
                    throw new IllegalStateException("Can't register node in databse!");
                }
            }
        }
    }

    private void initSupervisors() {
        activitySupervisor = new ActivitySupervisor();
        repairSupervisor = new RepairSupervisor();

        logger.info("Starting {} ...", ACTIVITY_SUPERVISOR_NAME);
        activitySupervisor.start();
        logger.info("Starting {} ...", REPAIR_SUPERVISOR_NAME);
        repairSupervisor.start();
    }

    public List<? extends NodeEntity> getActiveNodes() {
        return queryActive.prepare().setParameter(1, System.currentTimeMillis()).selectList();
    }

    public List<? extends Node> getPassiveNodes() {
        return queryPassive.prepare().setParameter(1, System.currentTimeMillis()).selectList();
    }

    public List<? extends Node> getNodes() {
        return queryAll.prepare().setParameter(1, System.currentTimeMillis()).selectList();
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    public void registerModule(IClusteredModule module) {
        try {
            em.create(new ModuleEntity(currentNode.getId(), module.getName()));
        } catch (UpdateException e) {
            logger.debug("Module {} already registered in database for node {}", module.getName(), currentNode.getId());
        }

        modules.put(module.getName(), module);
    }

    public final class ActivitySupervisor extends Thread {
        private boolean isAlive = false;

        public ActivitySupervisor() {
            super(ACTIVITY_SUPERVISOR_NAME);
            this.setDaemon(true);
        }

        @Override
        public void run() {
            logger.info("Started {}", ACTIVITY_SUPERVISOR_NAME);
            while (!isInterrupted()) {
                tm.begin();
                try {
                    long start = System.currentTimeMillis();

                    if (!lockCurrentNode()) {
                        if (isAlive) {
                            isAlive = false;
                            stopModules();
                        }

                    } else {
                        waitForModulesLock();

                        if (!isAlive) {
                            isAlive = true;
                            startModules();
                        }
                    }
                    long end = System.currentTimeMillis();
                    sleep(Math.max(0, lockTimeout - (end - start)));
                    updateCurrentNodeActivity(end);
                } catch (InterruptedException e) {
                    break;
                } finally {
                    tm.commit();
                }
            }
            logger.info("Stopped {}", ACTIVITY_SUPERVISOR_NAME);
        }

        private void updateCurrentNodeActivity(long end) {
            currentNode.setLastActivity(end + failoverTimeout);
            try {
                em.update(currentNode);
            } catch (UpdateException e) {
                logger.error("Can't encrease activity timestamp for " + currentNode.getId(), e);
            }
        }

        private void waitForModulesLock() {
            List<ModuleEntity> moduleEntities = queryModules.prepare().setParameter("nodeId", currentNode.getId()).setParameter("name", modules.keySet()).selectList();
            em.lock(LockType.WAIT, moduleEntities);
        }

        private boolean lockCurrentNode() {
            try {
                em.update(currentNode);
            } catch (Exception e) {
                logger.error("Can't update current node " + currentNode.getId(), e);
                return false;
            }
            return true;
        }

        private void startModules() {
            for (IClusteredModule cm : modules.values()) {
                logger.info("Starting module {}", cm.getName());
                try {
                    cm.handleStart();
                } catch (Throwable e) {
                    logger.error("Can't start module " + cm.getName(), e);
                }
            }
        }

        private void stopModules() {
            for (IClusteredModule cm : modules.values()) {
                logger.info("Stopping module {}", cm.getName());
                try {
                    cm.handleStart();
                } catch (Throwable e) {
                    logger.error("Can't stop module " + cm.getName(), e);
                }
            }

        }

    }

    public final class RepairSupervisor extends Thread {
        public RepairSupervisor() {
            super(REPAIR_SUPERVISOR_NAME);
            this.setDaemon(true);
        }

        @Override
        public void run() {
            logger.info("Started {}", ACTIVITY_SUPERVISOR_NAME);
            logger.info("Stopped {}", ACTIVITY_SUPERVISOR_NAME);
        }
    }

}
