package ru.kwanza.jeda.persistentqueue.jdbc;

import ru.kwanza.dbtool.orm.api.IEntityManager;
import ru.kwanza.dbtool.orm.api.IQuery;
import ru.kwanza.dbtool.orm.api.IQueryBuilder;
import ru.kwanza.dbtool.orm.api.If;
import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.jeda.persistentqueue.IPersistableEvent;
import ru.kwanza.jeda.persistentqueue.IQueuePersistenceController;
import ru.kwanza.toolbox.fieldhelper.FieldHelper;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class JDBCQueuePersistenceController<E extends IPersistableEvent> implements IQueuePersistenceController<E> {
    private final FieldHelper.Field<Object, E> eventField;
    private final Class ormClass;
    private final If determinator;
    private final IEntityManager em;
    private final IQuery loadQuery;

    public JDBCQueuePersistenceController(IEntityManager em, Class ormClass,
                                          String idField, String nodeIdField, String eventField, If determinator) {
        this.ormClass = ormClass;
        this.determinator = determinator;
        this.em = em;
        this.eventField = FieldHelper.construct(ormClass,eventField);
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
    }

    public void persist(Collection<E> events, Node node) {

    }

    public int transfer(int count, Node currentNode, Node repairableNode) {
        return 0;
    }
}