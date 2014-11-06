package ru.kwanza.jeda.persistentqueue.berkeley;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.jeda.jeconnection.JEConnectionException;
import ru.kwanza.jeda.jeconnection.JEConnectionFactory;
import ru.kwanza.jeda.persistentqueue.EventWithKey;
import ru.kwanza.jeda.persistentqueue.PersistenceQueueException;
import ru.kwanza.jeda.persistentqueue.IQueuePersistenceController;
import ru.kwanza.jeda.persistentqueue.springintegration.TestNonSerializableEvent;
import ru.kwanza.toolbox.SerializationHelper;

import javax.annotation.Resource;
import javax.resource.ResourceException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Kiryl Karatsetski
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class TestBerkeleyPersistenceController extends AbstractJUnit4SpringContextTests {

    @Autowired
    private IJedaManager systemManager;
    @Resource(name = "bpqController")
    private IQueuePersistenceController controller;
    @Resource(name = "connectionFactory")
    private JEConnectionFactory factoryJE;
    @Resource(name = "connectionFactory2")
    private JEConnectionFactory factoryJE2;

    private Node node0 = new Node(0);
    private Node node1 = new Node(1);
    @Before
    public void setUp() throws Exception {
        delete(new File("./target/berkeley_db"));
    }

    @Test
    public void testCreateEnv() throws ResourceException, SystemException, RollbackException {
        systemManager.getTransactionManager().begin();
        ArrayList<TestEvent> list0 = new ArrayList<TestEvent>();
        for (long i = 0; i < 1000; i++) {
            list0.add(new TestEvent(i,String.valueOf(i)));
        }
        controller.persist(list0, node0);
        systemManager.getTransactionManager().commit();

        systemManager.getTransactionManager().begin();
        ArrayList<TestEvent> list1 = new ArrayList<TestEvent>();
        for (long i = 1000; i < 2000; i++) {
            list1.add(new TestEvent(i,String.valueOf(i)));
        }
        controller.persist(list1, node1);
        systemManager.getTransactionManager().commit();

        systemManager.getTransactionManager().begin();
        Collection<TestEvent> load0 = controller.load(100000,node0);
        systemManager.getTransactionManager().commit();
        Assert.assertEquals(1000, load0.size());

        systemManager.getTransactionManager().begin();
        Collection<EventWithKey> load1 = controller.load(10000,node1);
        systemManager.getTransactionManager().commit();
        Assert.assertEquals(1000, load1.size());

        systemManager.getTransactionManager().begin();
        int count = controller.transfer(500, node0, node1);
        systemManager.getTransactionManager().commit();
        Assert.assertEquals(500, count);

        systemManager.getTransactionManager().begin();
        Collection<EventWithKey> load3 = controller.load(100000,node0);
        systemManager.getTransactionManager().commit();
        Assert.assertEquals(500, load3.size());

        systemManager.getTransactionManager().begin();
        Collection<EventWithKey> load4 = controller.transfer(300, 0, 1);
        systemManager.getTransactionManager().commit();
        Assert.assertEquals(300, load4.size());

        systemManager.getTransactionManager().begin();
        Collection<EventWithKey> load5 = controller.load(0);
        systemManager.getTransactionManager().commit();
        Assert.assertEquals(200, load5.size());

        systemManager.getTransactionManager().begin();
        Collection<EventWithKey> load6 = controller.transfer(500, 0, 1);
        systemManager.getTransactionManager().commit();
        Assert.assertEquals(200, load6.size());

        systemManager.getTransactionManager().begin();
        Collection<EventWithKey> load7 = controller.load(0);
        systemManager.getTransactionManager().commit();
        Assert.assertEquals(0, load7.size());

        factoryJE.closeConnection(0);
        factoryJE.closeConnection(1);

        systemManager.getTransactionManager().begin();
        Collection<EventWithKey> load8 = controller.load(1);
        systemManager.getTransactionManager().commit();
        Assert.assertEquals(2000, load8.size());

        systemManager.getTransactionManager().begin();
        controller.delete(load8, 1);
        systemManager.getTransactionManager().commit();

        systemManager.getTransactionManager().begin();
        Collection<EventWithKey> load9 = controller.load(1);
        systemManager.getTransactionManager().commit();
        Assert.assertEquals(0, load7.size());

        factoryJE.closeConnection(0);
        factoryJE.closeConnection(1);
    }

    @Test
    public void testFailPersist() throws ResourceException, SystemException, RollbackException {
        systemManager.getTransactionManager().begin();
        ArrayList<EventWithKey> list0 = new ArrayList<EventWithKey>();
        for (int i = 0; i < 1000; i++) {
            list0.add(new EventWithKey(new TestNonSerializableEvent()));
        }
        try {
            controller.persist(list0, node0);
            Assert.fail("Expected " + PersistenceQueueException.class);
        } catch (PersistenceQueueException e) {
        } finally {
            systemManager.getTransactionManager().rollback();
        }

        factoryJE.closeConnection(0);
        factoryJE.closeConnection(1);
    }

    @Test
    public void testFailPersist_1() throws ResourceException, SystemException, RollbackException {
        factoryJE2.getConnection(0);
        systemManager.getTransactionManager().begin();
        ArrayList<EventWithKey> list0 = new ArrayList<EventWithKey>();
        for (int i = 0; i < 1000; i++) {
            list0.add(new EventWithKey(new TestNonSerializableEvent()));
        }
        try {
            controller.persist(list0, node0);
            Assert.fail("Expected " + PersistenceQueueException.class);
        } catch (PersistenceQueueException e) {
        } finally {
            systemManager.getTransactionManager().rollback();
        }

        factoryJE.closeConnection(0);
        factoryJE2.closeConnection(0);
        factoryJE.closeConnection(1);
        factoryJE2.closeConnection(1);
    }

    @Test
    public void testFailLoad() throws ResourceException, SystemException, RollbackException {
        systemManager.getTransactionManager().begin();
        Database database = factoryJE.getConnection(0)
                .openDatabase("test_db", new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        database.put(null, new DatabaseEntry(SerializationHelper.longToBytes(11111)),
                new DatabaseEntry(SerializationHelper.longToBytes(11111)));
        systemManager.getTransactionManager().commit();

        systemManager.getTransactionManager().begin();
        try {
            controller.load(0);
            Assert.fail("Expected " + PersistenceQueueException.class);
        } catch (PersistenceQueueException e) {
        } finally {
            systemManager.getTransactionManager().rollback();
        }

        factoryJE.closeConnection(0);
        factoryJE.closeConnection(1);
    }

    @Test
    public void testFailLoad_1() throws ResourceException, SystemException, RollbackException {
        factoryJE2.getConnection(0);
        systemManager.getTransactionManager().begin();

        try {
            controller.load(0);
            Assert.fail("Expected " + PersistenceQueueException.class);
        } catch (PersistenceQueueException e) {
        } finally {
            systemManager.getTransactionManager().rollback();
        }
        factoryJE.closeConnection(0);
        factoryJE2.closeConnection(0);
        factoryJE.closeConnection(1);
        factoryJE2.closeConnection(1);
    }

    @Test
    public void testFailDelete() throws ResourceException, SystemException, RollbackException {
        factoryJE2.getConnection(0);
        systemManager.getTransactionManager().begin();

        try {
            controller.delete(Arrays.asList(new EventWithKey(new TestEvent("Test"))), 0);
            Assert.fail("Expected " + PersistenceQueueException.class);
        } catch (PersistenceQueueException e) {
        } finally {
            systemManager.getTransactionManager().rollback();
        }
        factoryJE.closeConnection(0);
        factoryJE2.closeConnection(0);
        factoryJE.closeConnection(1);
        factoryJE2.closeConnection(1);
    }

    @Test
    public void testFailTransfer() throws ResourceException, SystemException, RollbackException {
        factoryJE2.getConnection(0);
        systemManager.getTransactionManager().begin();

        try {
            controller.transfer(1000, 0, 1);
            Assert.fail("Expected " + PersistenceQueueException.class);
        } catch (PersistenceQueueException e) {
        } finally {
            systemManager.getTransactionManager().rollback();
        }
        factoryJE.closeConnection(0);
        factoryJE2.closeConnection(0);
        factoryJE.closeConnection(1);
        factoryJE2.closeConnection(1);
    }

    @Test
    public void testJEConnectionFail() {
        try {
            factoryJE.getTxConnection(0);
            Assert.fail("Expected " + JEConnectionException.class);
        } catch (JEConnectionException e) {
        }

        factoryJE.destroy();

    }

    @Test
    public void testPersists_Rollback() throws ResourceException, SystemException, RollbackException {
        systemManager.getTransactionManager().begin();
        ArrayList<EventWithKey> list0 = new ArrayList<EventWithKey>();
        for (int i = 0; i < 1000; i++) {
            list0.add(new EventWithKey(new TestEvent(String.valueOf(i))));
        }
        controller.persist(list0, 0);
        systemManager.getTransactionManager().rollback();

        systemManager.getTransactionManager().begin();
        ArrayList<EventWithKey> list1 = new ArrayList<EventWithKey>();
        for (int i = 1000; i < 2000; i++) {
            list1.add(new EventWithKey(new TestEvent(String.valueOf(i))));
        }
        controller.persist(list1, 0);
        systemManager.getTransactionManager().commit();

        factoryJE.destroy();
    }

    private void delete(File file) throws IOException {
        if (file.isDirectory()) {
            for (File item : file.listFiles()) {
                delete(item);
            }
        }
        file.delete();
    }
}
