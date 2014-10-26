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

/**
 * @author Guzanov Alexander
 */
public class JDBCQueuePersistenceController<E extends IPersistableEvent,R extends IEventRecord> implements IQueuePersistenceController<E> {
    private final FieldHelper.Field<R, E> eventField;
    private final FieldHelper.Field<E, Long> persistIdField = new FieldHelper.Field<E, Long>() {
        public Long value(E o) {
            return o.getPersistId();
        }
    };

    private final Class<R> ormClass;
    private final If determinator;
    private final IEntityManager em;
    private final IQuery loadQuery;

    public JDBCQueuePersistenceController(IEntityManager em,
                                          Class<R> ormClass,
                                          String idField,
                                          String nodeIdField,
                                          If determinator) {
        this.ormClass = ormClass;
        this.determinator = determinator;
        this.em = em;
        this.eventField = FieldHelper.construct(ormClass, "event");

        IQueryBuilder queryBuilder = em.queryBuilder(ormClass);
        If condition = determinator == null ? If.isEqual(nodeIdField) : If.and(If.isEqual(nodeIdField), determinator);
        loadQuery = queryBuilder.where(condition).orderBy(idField).create();
    }

    public String getQueueName() {
        return ormClass.getName() + (determinator == null ? "" : determinator.toString());
    }

    public int getTotalCount(Node node) {
        return 0;
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

    }

    public int transfer(int count, Node currentNode, Node repairableNode) {
        return 0;
    }
}