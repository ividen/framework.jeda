package ru.kwanza.jeda.persistentqueue.jdbc;

import ru.kwanza.dbtool.core.UpdateException;
import ru.kwanza.dbtool.orm.api.IEntityManager;
import ru.kwanza.dbtool.orm.api.IQuery;
import ru.kwanza.dbtool.orm.api.IQueryBuilder;
import ru.kwanza.dbtool.orm.api.If;
import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.jeda.persistentqueue.IPersistableEvent;
import ru.kwanza.jeda.persistentqueue.IQueuePersistenceController;
import ru.kwanza.toolbox.fieldhelper.FieldHelper;

import java.util.Collection;
import java.util.List;

/**
 * @author Guzanov Alexander
 */
public class JDBCQueuePersistenceController<E extends IPersistableEvent, R extends IEventRecord> implements IQueuePersistenceController<E> {
    private FieldHelper.Field<R, E> eventField;
    private FieldHelper.Field<E, Long> persistIdField = new FieldHelper.Field<E, Long>() {
        public Long value(E o) {
            return o.getPersistId();
        }
    };
    private FieldHelper.Field<E, R> buildField;

    private final IEntityManager em;
    private final Class<R> ormClass;
    private final IEventRecordBuilder<R, E> builder;
    private final If determinator;
    private final IQuery loadQuery;


    public JDBCQueuePersistenceController(IEntityManager em,
                                          Class<R> ormClass,
                                          IEventRecordBuilder<R, E> builder,
                                          If determinator,
                                          String idField,
                                          String nodeIdField) {
        this.ormClass = ormClass;
        this.builder = builder;
        this.determinator = determinator;
        this.em = em;
        initFields();

        IQueryBuilder queryBuilder = em.queryBuilder(ormClass);
        If condition = determinator == null ? If.isEqual(nodeIdField) : If.and(If.isEqual(nodeIdField), determinator);
        loadQuery = queryBuilder.where(condition).orderBy(idField).create();
    }

    private void initFields() {
        this.eventField = FieldHelper.construct(ormClass, "event");
        this.buildField = new FieldHelper.Field<E, R>() {
            public R value(E event) {
                return builder.build(event);
            }
        };
    }

    public String getQueueName() {
        return JDBCQueuePersistenceController.class.getSimpleName() + "."
                + ormClass.getName() + (determinator == null ? "" : determinator.toString());
    }

    public int getTotalCount(Node node) {
        //todo aguzanov: not very good;
        List list = loadQuery.prepare().setParameter(1, node.getId()).selectList();
        return list.size();
    }

    public Collection<E> load(int count, Node node) {
        return FieldHelper
                .getFieldCollection(loadQuery.prepare()
                        .paging(0, count).setParameter(1, node.getId()).selectList(), eventField);
    }

    public void delete(Collection<E> result, Node node) {
        try {
            em.deleteByKeys(ormClass, FieldHelper.getFieldCollection(result, persistIdField));
        } catch (UpdateException e) {
            throw new RuntimeException(e);
        }
    }

    public void persist(Collection<E> events, Node node) {
        try {
            em.create(ormClass, FieldHelper.getFieldCollection(events, buildField));
        } catch (UpdateException e) {
            e.printStackTrace();
        }
    }

    public int transfer(int count, Node currentNode, Node repairableNode) {
        List<R> list = loadQuery.prepare().paging(0, count).setParameter(1, repairableNode.getId()).selectList();

        for (R e : list) {
            e.setNodeId(currentNode.getId());
        }
        try {
            em.update(ormClass, list);
        } catch (UpdateException e) {
            throw new RuntimeException(e);
        }

        return list.size();
    }
}