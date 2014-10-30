package ru.kwanza.jeda.persistentqueue.jdbc;

import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import ru.kwanza.jeda.persistentqueue.IQueuePersistenceController;

import javax.annotation.Resource;

/**
 * @author Alexander Guzanov
 */
@ContextConfiguration("test-config.xml")
public class TestJdbcPersistentController  extends AbstractTransactionalJUnit4SpringContextTests {
    @Resource(name = "defaultQueueController")
    private IQueuePersistenceController controller;

    @Test
    public void testPersist(){
        controller.persist();
    }

    @Test
    public void testDelete(){
        controller.delete();
    }

    @Test
    public void testLoad(){
        controller.load();
    }

    @Test
    public void testLoad(){
        controller.load();
    }

    @Test
    public void testTransfer(){
        controller.transfer();
    }
}

