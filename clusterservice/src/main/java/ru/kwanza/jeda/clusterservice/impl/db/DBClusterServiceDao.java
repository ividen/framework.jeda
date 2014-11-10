package ru.kwanza.jeda.clusterservice.impl.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kwanza.dbtool.core.UpdateException;
import ru.kwanza.dbtool.orm.api.IEntityManager;
import ru.kwanza.dbtool.orm.api.IQuery;
import ru.kwanza.dbtool.orm.api.If;
import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.jeda.clusterservice.impl.db.orm.ClusterNode;
import ru.kwanza.jeda.clusterservice.impl.db.orm.ClusteredComponent;
import ru.kwanza.jeda.clusterservice.impl.db.orm.WaitForReturnComponent;
import ru.kwanza.toolbox.fieldhelper.FieldHelper;
import ru.kwanza.txn.api.spi.ITransactionManager;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * @author Alexander Guzanov
 */
public class DBClusterServiceDao {
    private static Logger logger = LoggerFactory.getLogger(DBClusterService.class);

    @Resource(name = "dbtool.IEntityManager")
    private IEntityManager em;
    @Resource
    private ComponentRepository repository;

    private IQuery<ClusterNode> queryActive;
    private IQuery<ClusterNode> queryPassive;
    private IQuery<ClusterNode> queryAll;
    private IQuery<ClusteredComponent> queryForComponents;
    private IQuery<ClusteredComponent> queryForAlienStale;

    @PostConstruct
    public void init() {
        queryActive = em.queryBuilder(ClusterNode.class).where(If.isGreater("lastActivity")).create();
        queryPassive = em.queryBuilder(ClusterNode.class).where(If.isLessOrEqual("lastActivity")).create();
        queryAll = em.queryBuilder(ClusterNode.class).create();
        queryForComponents = em.queryBuilder(ClusteredComponent.class)
                .where(
                        If.and(
                                If.isEqual("nodeId"),
                                If.in("name"))
                ).create();

        queryForAlienStale = em.queryBuilder(ClusteredComponent.class)
                .lazy()
                .where(
                        If.and(
                                If.notEqual("nodeId"),
                                If.in("name"),
                                If.isLessOrEqual("lastActivity"),
                                If.isEqual("repaired", If.valueOf(false))
                        )).create();
    }


    public ClusterNode findOrCreateNode(ClusterNode node) {
        if (em.readByKey(ClusterNode.class, node.getId()) == null) {
            try {
                em.create(node);
            } catch (UpdateException e) {
                if (em.readByKey(ClusterNode.class, node.getId()) == null) {
                    throw new IllegalStateException("Can't register node in database!");
                }
            }
        }

        return node;
    }

    public ClusteredComponent findOrCreateComponent(ClusterNode node, IClusteredComponent component) {
        ClusteredComponent result;

        if ((result = em.readByKey(ClusteredComponent.class, ClusteredComponent.createId(node.getId(), component.getName()))) == null) {
            try {
                result = em.create(new ClusteredComponent(node.getId(), component.getName()));
            } catch (UpdateException e) {
                logger.debug("Module {} already registered in database for node {}", component.getName(), node.getId());
            }
        }

        return result;
    }

    public Collection<ClusteredComponent> loadComponentsByKey(Collection<String> keys) {
        Collection<ClusteredComponent> items = em.readByKeys(ClusteredComponent.class, keys);
        em.fetchLazy(ClusteredComponent.class, items);

        return items;
    }


    public void updateComponents(Collection<ClusteredComponent> items) throws UpdateException {
        em.update(ClusteredComponent.class, items);
    }

    public List<? extends ClusterNode> selectActiveNodes() {
        return queryActive.prepare().setParameter(1, System.currentTimeMillis()).selectList();
    }

    public List<? extends Node> selectPassiveNodes() {
        return queryPassive.prepare().setParameter(1, System.currentTimeMillis()).selectList();
    }

    public List<? extends Node> selectNodes() {
        return queryAll.prepare().setParameter(1, System.currentTimeMillis()).selectList();
    }


    public List<ClusteredComponent> selectAlienStaleComponents(Node node) {
        return queryForAlienStale.prepare()
                .setParameter(1, node.getId())
                .setParameter(2, repository.getStartedComponents().keySet())
                .setParameter(3, System.currentTimeMillis())
                .selectList();
    }

    public List<ClusteredComponent> selectAllComponents(Node node) {
        return queryForComponents.prepare()
                .setParameter(1, node.getId())
                .setParameter(2, repository.getComponents().keySet())
                .selectList();
    }

    public Collection<ClusteredComponent> selectWaitForReturn() {
        return em.readByKeys(ClusteredComponent.class,
                FieldHelper.getFieldCollection(repository.getPassiveComponents(),
                        FieldHelper.construct(ClusteredComponent.class, "id")));
    }


    public void markWaitForReturn() {
        try {
            em.update(WaitForReturnComponent.class, FieldHelper.getFieldCollection(repository.getPassiveComponents(),
                    FieldHelper.<ClusteredComponent, WaitForReturnComponent>construct(ClusteredComponent.class, "waitEntity")));
        } catch (UpdateException e) {
            //todo aguzanov log error;
        }
    }

}
