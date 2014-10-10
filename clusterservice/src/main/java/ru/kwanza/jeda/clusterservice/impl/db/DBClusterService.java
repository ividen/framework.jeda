package ru.kwanza.jeda.clusterservice.impl.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kwanza.dbtool.core.UpdateException;
import ru.kwanza.dbtool.orm.api.*;
import ru.kwanza.jeda.clusterservice.IClusterService;
import ru.kwanza.jeda.clusterservice.IClusteredModule;
import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.jeda.clusterservice.impl.db.orm.ModuleEntity;
import ru.kwanza.jeda.clusterservice.impl.db.orm.NodeEntity;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author Alexander Guzanov
 */
public class DBClusterService implements IClusterService {
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

    @PostConstruct
    public void init() {
        queryActive = em.queryBuilder(NodeEntity.class).where(If.isGreater("lastActivity")).create();
        queryPassive = em.queryBuilder(NodeEntity.class).where(If.isLessOrEqual("lastActivity")).create();
        queryAll = em.queryBuilder(NodeEntity.class).create();
        queryModules = em.queryBuilder(ModuleEntity.class).where(If.isEqual("nodeId")).create();
        currentNode = new NodeEntity(Integer.valueOf(System.getProperty("clusterservice.nodeId")), System.currentTimeMillis());
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
    }

    public final class ActivitySupervisor extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()) {
                LockResult<NodeEntity> lock = em.lock(LockType.WAIT, currentNode);
                if (!lock.getLocked().isEmpty()) {
                    List<ModuleEntity> modules = queryModules.prepare().setParameter(1, currentNode.getId()).selectList();
                    LockResult<ModuleEntity> result = em.lock(LockType.WAIT, ModuleEntity.class, modules);
                    result.getLocked()


                    try {
                        sleep(lockTimeout);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }

        public final class RepairSupervisor extends Thread {

        }
    }

}
