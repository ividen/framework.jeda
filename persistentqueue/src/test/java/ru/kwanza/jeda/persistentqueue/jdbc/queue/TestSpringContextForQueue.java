package ru.kwanza.jeda.persistentqueue.jdbc.queue;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
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
public class TestSpringContextForQueue extends AbstractTransactionalJUnit4SpringContextTests {
    @Autowired
    private IEntityManager em;
    @Resource(name = "dbtool.IEntityMappingRegistry")
    private IEntityMappingRegistry registry;

    @Test
    public void testEntities() {
        Assert.assertTrue(registry.isRegisteredEntityClass(EventQueue.class));
        Assert.assertTrue(registry.isRegisteredEntityClass(NamedEventQueue.class));
        Assert.assertTrue(registry.isRegisteredEntityClass(PriorityEventQueue.class));
        Assert.assertTrue(registry.isRegisteredEntityClass(NamedPriorityEventQueue.class));

        Assert.assertEquals("jeda_jdbc_event_queue", registry.getEntityType(EventQueue.class).getTableName());
        Assert.assertEquals("jeda.persistentqueue.jdbc.EventQueue", registry.getEntityType(EventQueue.class).getName());
        Assert.assertEquals("jeda_jdbc_event_nqueue", registry.getEntityType(NamedEventQueue.class).getTableName());
        Assert.assertEquals("jeda.persistentqueue.jdbc.NamedEventQueue", registry.getEntityType(NamedEventQueue.class).getName());
        Assert.assertEquals("jeda_jdbc_event_pqueue", registry.getEntityType(PriorityEventQueue.class).getTableName());
        Assert.assertEquals("jeda.persistentqueue.jdbc.PriorityEventQueue", registry.getEntityType(PriorityEventQueue.class).getName());
        Assert.assertEquals("jeda_jdbc_event_pnqueue", registry.getEntityType(NamedPriorityEventQueue.class).getTableName());
        Assert.assertEquals("jeda.persistentqueue.jdbc.NamedPriorityEventQueue", registry.getEntityType(NamedPriorityEventQueue.class).getName());
    }

    @Test
    public void testTables() throws UpdateException {
        em.create(new EventQueue.Helper().buildRecord(new DefaultPersistableEvent(1l), 1));
        Assert.assertEquals(1l,em.readByKey(EventQueue.class,1l).getId().longValue());
        em.create(new NamedEventQueue.Helper("test_queue").buildRecord(new DefaultPersistableEvent(1l), 1));
        Assert.assertEquals(1l,em.readByKey(NamedEventQueue.class,1l).getId().longValue());
        em.create(new PriorityEventQueue.Helper()
                .buildRecord(new DefaultPriorityPersistableEvent(1l, IPriorityEvent.Priority.CRITICAL), 1));
        Assert.assertEquals(1l,em.readByKey(PriorityEventQueue.class,1l).getId().longValue());
        em.create(new NamedPriorityEventQueue.Helper("test_queue")
                .buildRecord(new DefaultPriorityPersistableEvent(1l, IPriorityEvent.Priority.CRITICAL), 1));
        Assert.assertEquals(1l,em.readByKey(NamedPriorityEventQueue.class,1l).getId().longValue());

    }

}
