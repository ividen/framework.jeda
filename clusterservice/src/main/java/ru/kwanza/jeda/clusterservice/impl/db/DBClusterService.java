package ru.kwanza.jeda.clusterservice.impl.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kwanza.dbtool.core.UpdateException;
import ru.kwanza.dbtool.orm.api.IEntityManager;
import ru.kwanza.dbtool.orm.api.IQuery;
import ru.kwanza.dbtool.orm.api.If;
import ru.kwanza.jeda.clusterservice.IClusterService;
import ru.kwanza.jeda.clusterservice.IClusteredModule;
import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.jeda.clusterservice.impl.db.orm.ModuleEntity;
import ru.kwanza.jeda.clusterservice.impl.db.orm.NodeEntity;

import javax.annotation.PostConstruct;
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
    private ActivitySupervisor supervisor;
    private RepairSupervisor repairSupervisor;
    private ConcurrentMap<String, IClusteredModule> modules = new ConcurrentHashMap<String, IClusteredModule>();

    @PostConstruct
    public void init() {
        initQuery();
        initCurrentNode();
        initSupervisors();
    }

    private void initQuery() {
        queryActive = em.queryBuilder(NodeEntity.class).where(If.isGreater("lastActivity")).create();
        queryPassive = em.queryBuilder(NodeEntity.class).where(If.isLessOrEqual("lastActivity")).create();
        queryAll = em.queryBuilder(NodeEntity.class).create();
        queryModules = em.queryBuilder(ModuleEntity.class).where(If.isEqual("nodeId")).create();
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
        supervisor = new ActivitySupervisor();
        repairSupervisor = new RepairSupervisor();

        supervisor.start();
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
            while (!isInterrupted()) {
                currentNode.setLastActivity(System.currentTimeMillis() + lockTimeout + failoverTimeout);
                try {
                    em.update(currentNode);
                } catch (Exception e) {
                    logger.error("Can't update current node " + currentNode.getId(), e);

                    if (isAlive) {
                        for (IClusteredModule cm : modules.values()) {
                            logger.info("Stopping module {}", cm.getName());
                            cm.handleStart();
                        }
                    }
                }

                if (!isAlive) {
                    isAlive = true;
                    for (IClusteredModule cm : modules.values()) {
                        logger.info("Starting module {}", cm.getName());
                        cm.handleStart();
                    }
                }

                try {
                    sleep(lockTimeout);
                } catch (InterruptedException e) {
                    break;
                }
            }
            logger.info("{} stopped", ACTIVITY_SUPERVISOR_NAME);
        }

    }

    public final class RepairSupervisor extends Thread {
        public RepairSupervisor() {
            super(REPAIR_SUPERVISOR_NAME);
            this.setDaemon(true);
        }

        @Override
        public void run() {
            logger.info("{} stopped", ACTIVITY_SUPERVISOR_NAME);
        }
    }

}
