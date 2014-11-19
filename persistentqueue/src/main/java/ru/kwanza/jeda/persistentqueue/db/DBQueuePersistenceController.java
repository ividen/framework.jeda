package ru.kwanza.jeda.persistentqueue.db;

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
public class DBQueuePersistenceController<E extends IPersistableEvent, R extends IEventRecord> implements IQueuePersistenceController<E> {
    private FieldHelper.Field<R, E> eventField;
    private FieldHelper.Field<E, Long> persistIdField = new FieldHelper.Field<E, Long>() {
        public Long value(E o) {
            return o.getPersistId();
        }
    };

    private final IEntityManager em;
    private final IEventRecordHelper<R, E> builder;
    private final If determinator;
    private final IQuery loadQuery;


    public DBQueuePersistenceController(IEntityManager em,
                                        IEventRecordHelper<R, E> builder) {
        this.builder = builder;
        this.em = em;
        initFields();

        IQueryBuilder queryBuilder = em.queryBuilder(builder.getORMClass());
        this.determinator = builder.getQueueNameField() == null || builder.getQueueNameValue() == null
                ? null : If.isEqual(builder.getQueueNameField(), If.valueOf(builder.getQueueNameValue()));
        If condition = determinator == null ? If.isEqual(builder.getNodeIdField()) : If.and(If.isEqual(builder.getNodeIdField()), determinator);
        loadQuery = queryBuilder.where(condition).orderBy(builder.getIdField()).create();
    }

    private void initFields() {
        this.eventField = FieldHelper.construct(builder.getORMClass(), "eventData");
    }

    public String getQueueName() {
        return DBQueuePersistenceController.class.getSimpleName() + "."
                + builder.getORMClass().getName() + (builder.getQueueNameField() == null || builder.getQueueNameValue() == null ?
                "" : ":" + builder.getQueueNameField() + "=" + builder.getQueueNameValue());
    }

    public Collection<E> load(int count, Node node) {
        return FieldHelper
                .getFieldCollection(loadQuery.prepare()
                        .paging(0, count).setParameter(1, node.getId()).selectList(), eventField);
    }

    public void delete(Collection<E> result, Node node) {
        try {
            em.deleteByKeys(builder.getORMClass(), FieldHelper.getFieldCollection(result, persistIdField));
        } catch (UpdateException e) {
            throw new RuntimeException(e);
        }
    }

    public void persist(Collection<E> events, final Node node) {
        try {
            em.create(builder.getORMClass(), FieldHelper.getFieldCollection(events, new FieldHelper.Field<E, R>() {
                public R value(E event) {
                    return builder.buildRecord(event, node.getId());
                }
            }));
        } catch (UpdateException e) {
            e.printStackTrace();
        }
    }
}