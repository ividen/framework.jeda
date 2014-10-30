package ru.kwanza.jeda.persistentqueue.jdbc;

import junit.framework.Assert;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.jeda.persistentqueue.DefaultPersistableEvent;
import ru.kwanza.jeda.persistentqueue.IQueuePersistenceController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Alexander Guzanov
 */
@ContextConfiguration("test-config.xml")
public class TestJdbcPersistentController extends AbstractTransactionalJUnit4SpringContextTests {
    @Resource(name = "defaultQueueController")
    private IQueuePersistenceController<DefaultPersistableEvent> controller;
    private Node node1 = new Node(1);
    private Node node2 = new Node(1);

    @Test
    public void testPersist() {
        controller.persist(Arrays.asList(new DefaultPersistableEvent(1l)), node1);
        controller.persist(Arrays.asList(new DefaultPersistableEvent(2l)), node2);
    }

    @Test
    public void testDelete() {
        controller.delete(Arrays.asList(new DefaultPersistableEvent(3l)), node1);
        controller.delete(Arrays.asList(new DefaultPersistableEvent(13l)), node2);
    }

    @Test
    public void testLoad() {
        Collection<DefaultPersistableEvent> load = controller.load(10, node1);
        Assert.assertEquals(10, load.size());
        long i = 3;
        for (DefaultPersistableEvent de : load) {
            Assert.assertEquals(i, de.getPersistId().longValue());
            i++;
        }

        load = controller.load(5, node1);
        Assert.assertEquals(5, load.size());
        i = 13;
        for (DefaultPersistableEvent de : load) {
            Assert.assertEquals(i, de.getPersistId().longValue());
            i++;
        }

        load = controller.load(100, node1);
        Assert.assertEquals(5, load.size());
        for (DefaultPersistableEvent de : load) {
            Assert.assertEquals(i, de.getPersistId().longValue());
            i++;
        }
    }

    @Test
    public void testGetCount() {
        Assert.assertEquals(10, controller.getTotalCount(node1));
        Assert.assertEquals(10, controller.getTotalCount(node2));
    }

    @Test
    public void testTransfer_1() {
        Assert.assertEquals(1, controller.transfer(1, node1, node2));
    }

    @Test
    public void testTransfer_2() {
        Assert.assertEquals(10, controller.transfer(10, node1, node2));
    }

    @Test
    public void testTransfer_3() {
        Assert.assertEquals(10, controller.transfer(100, node1, node2));
    }
}

