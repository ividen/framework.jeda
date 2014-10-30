package ru.kwanza.jeda.persistentqueue.jdbc.queue;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import ru.kwanza.dbtool.core.UpdateException;
import ru.kwanza.dbtool.orm.api.IEntityManager;
import ru.kwanza.dbtool.orm.api.internal.IEntityMappingRegistry;
import ru.kwanza.jeda.api.IPriorityEvent;
import ru.kwanza.jeda.persistentqueue.DefaultPersistableEvent;
import ru.kwanza.jeda.persistentqueue.DefaultPriorityPersistableEvent;

import javax.annotation.Resource;

/**
 * @author Alexander Guzanov
 */
@ContextConfiguration("test-config.xml")
public class TestSpringContextForQueue extends AbstractJUnit4SpringContextTests {
    @Autowired
    private IEntityManager em;
    @Resource(name = "dbtool.IEntityMappingRegistry")
    private IEntityMappingRegistry registry;

    @Test
    public void testEntities() {
        Assert.assertTrue(registry.isRegisteredEntityClass(EventQueue.class));
        Assert.assertTrue(registry.isRegisteredEntityClass(EventQueueWithQueueName.class));
        Assert.assertTrue(registry.isRegisteredEntityClass(PriorityEventQueue.class));
        Assert.assertTrue(registry.isRegisteredEntityClass(PriorityEventQueueWithQueueName.class));

        Assert.assertEquals("jeda_jdbc_event_queue", registry.getEntityType(EventQueue.class).getTableName());
        Assert.assertEquals("jeda.persistentqueue.jdbc.EventQueue", registry.getEntityType(EventQueue.class).getName());
        Assert.assertEquals("jeda_jdbc_event_nqueue", registry.getEntityType(EventQueueWithQueueName.class).getTableName());
        Assert.assertEquals("jeda.persistentqueue.jdbc.EventQueueWithQueueName", registry.getEntityType(EventQueueWithQueueName.class).getName());
        Assert.assertEquals("jeda_jdbc_event_pqueue", registry.getEntityType(PriorityEventQueue.class).getTableName());
        Assert.assertEquals("jeda.persistentqueue.jdbc.PriorityEventQueue", registry.getEntityType(PriorityEventQueue.class).getName());
        Assert.assertEquals("jeda_jdbc_event_pnqueue", registry.getEntityType(PriorityEventQueueWithQueueName.class).getTableName());
        Assert.assertEquals("jeda.persistentqueue.jdbc.PriorityEventQueueWithQueueName", registry.getEntityType(PriorityEventQueueWithQueueName.class).getName());
    }

    @Test
    public void testTables() throws UpdateException {
        em.create(new EventQueue.Builder().build(new DefaultPersistableEvent(1l), 1));
        em.create(new EventQueueWithQueueName.Builder("test_queue").build(new DefaultPersistableEvent(1l), 1));
        em.create(new PriorityEventQueue.Builder()
                .build(new DefaultPriorityPersistableEvent(1l, IPriorityEvent.Priority.CRITICAL), 1));
        em.create(new PriorityEventQueueWithQueueName.Builder("test_queue")
                .build(new DefaultPriorityPersistableEvent(1l, IPriorityEvent.Priority.CRITICAL), 1));

    }

}
