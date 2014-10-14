package ru.kwanza.jeda.persistentqueue.berkeley;

import ru.kwanza.jeda.api.internal.ISystemManagerInternal;
import ru.kwanza.jeda.jeconnection.JEConnectionException;
import ru.kwanza.jeda.jeconnection.JEConnectionFactory;
import ru.kwanza.toolbox.SerializationHelper;
import ru.kwanza.jeda.persistentqueue.EventWithKey;
import ru.kwanza.jeda.persistentqueue.IQueuePersistenceController;
import ru.kwanza.jeda.persistentqueue.PersistenceQueueException;
import ru.kwanza.jeda.persistentqueue.springintegration.TestNonSerializableEvent;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import junit.framework.TestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
public abstract class TestBerkeleyPersistenceController extends TestCase {

    protected ClassPathXmlApplicationContext ctx;

    @Override
    public void setUp() throws Exception {
        delete(new File("./target/berkeley_db"));
        ctx = new ClassPathXmlApplicationContext(
                getContextName(), TestBerkeleyPersistenceController.class);
    }

    @Override
    public void tearDown() throws Exception {
        ctx.destroy();
    }

    public void testCreateEnv() throws ResourceException, SystemException, RollbackException {

        IQueuePersistenceController controller = (IQueuePersistenceController) ctx.getBean("bpqController");
        JEConnectionFactory factoryJE = (JEConnectionFactory) ctx.getBean("connectionFactory");
        ISystemManagerInternal systemManager = ctx.getBean(ISystemManagerInternal.class);
        systemManager.getTransactionManager().begin();
        ArrayList<EventWithKey> list0 = new ArrayList<EventWithKey>();
        for (int i = 0; i < 1000; i++) {
            list0.add(new EventWithKey(new TestEvent(String.valueOf(i))));
        }
        controller.persist(list0, 0);
        systemManager.getTransactionManager().commit();

        systemManager.getTransactionManager().begin();
        ArrayList<EventWithKey> list1 = new ArrayList<EventWithKey>();
        for (int i = 1000; i < 2000; i++) {
            list1.add(new EventWithKey(new TestEvent(String.valueOf(i))));
        }
        controller.persist(list1, 1);
        systemManager.getTransactionManager().commit();

        systemManager.getTransactionManager().begin();
        Collection<EventWithKey> load0 = controller.load(0);
        systemManager.getTransactionManager().commit();
        assertEquals(1000, load0.size());

        systemManager.getTransactionManager().begin();
        Collection<EventWithKey> load1 = controller.load(1);
        systemManager.getTransactionManager().commit();
        assertEquals(1000, load1.size());

        systemManager.getTransactionManager().begin();
        Collection<EventWithKey> load2 = controller.transfer(500, 0, 1);
        systemManager.getTransactionManager().commit();
        assertEquals(500, load2.size());

        systemManager.getTransactionManager().begin();
        Collection<EventWithKey> load3 = controller.load(0);
        systemManager.getTransactionManager().commit();
        assertEquals(500, load3.size());

        systemManager.getTransactionManager().begin();
        Collection<EventWithKey> load4 = controller.transfer(300, 0, 1);
        systemManager.getTransactionManager().commit();
        assertEquals(300, load4.size());

        systemManager.getTransactionManager().begin();
        Collection<EventWithKey> load5 = controller.load(0);
        systemManager.getTransactionManager().commit();
        assertEquals(200, load5.size());

        systemManager.getTransactionManager().begin();
        Collection<EventWithKey> load6 = controller.transfer(500, 0, 1);
        systemManager.getTransactionManager().commit();
        assertEquals(200, load6.size());

        systemManager.getTransactionManager().begin();
        Collection<EventWithKey> load7 = controller.load(0);
        systemManager.getTransactionManager().commit();
        assertEquals(0, load7.size());

        factoryJE.closeConnection(0l);
        factoryJE.closeConnection(1l);

        systemManager.getTransactionManager().begin();
        Collection<EventWithKey> load8 = controller.load(1);
        systemManager.getTransactionManager().commit();
        assertEquals(2000, load8.size());

        systemManager.getTransactionManager().begin();
        controller.delete(load8, 1);
        systemManager.getTransactionManager().commit();

        systemManager.getTransactionManager().begin();
        Collection<EventWithKey> load9 = controller.load(1);
        systemManager.getTransactionManager().commit();
        assertEquals(0, load7.size());

        factoryJE.closeConnection(0l);
        factoryJE.closeConnection(1l);
    }

    protected abstract String getContextName();

    public void testFailPersist() throws ResourceException, SystemException, RollbackException {
        IQueuePersistenceController controller = (IQueuePersistenceController) ctx.getBean("bpqController");
        JEConnectionFactory factoryJE = (JEConnectionFactory) ctx.getBean("connectionFactory");
        ISystemManagerInternal systemManager = ctx.getBean(ISystemManagerInternal.class);
        systemManager.getTransactionManager().begin();
        ArrayList<EventWithKey> list0 = new ArrayList<EventWithKey>();
        for (int i = 0; i < 1000; i++) {
            list0.add(new EventWithKey(new TestNonSerializableEvent()));
        }
        try {
            controller.persist(list0, 0);
            fail("Expected " + PersistenceQueueException.class);
        } catch (PersistenceQueueException e) {
        } finally {
            systemManager.getTransactionManager().rollback();
        }

        factoryJE.closeConnection(0l);
        factoryJE.closeConnection(1l);
    }

    public void testFailPersist_1() throws ResourceException, SystemException, RollbackException {
        IQueuePersistenceController controller = (IQueuePersistenceController) ctx.getBean("bpqController");
        JEConnectionFactory factoryJE = (JEConnectionFactory) ctx.getBean("connectionFactory");
        JEConnectionFactory factoryJE2 = (JEConnectionFactory) ctx.getBean("connectionFactory2");
        ISystemManagerInternal systemManager = ctx.getBean(ISystemManagerInternal.class);
        factoryJE2.getConnection(0l);
        systemManager.getTransactionManager().begin();
        ArrayList<EventWithKey> list0 = new ArrayList<EventWithKey>();
        for (int i = 0; i < 1000; i++) {
            list0.add(new EventWithKey(new TestNonSerializableEvent()));
        }
        try {
            controller.persist(list0, 0);
            fail("Expected " + PersistenceQueueException.class);
        } catch (PersistenceQueueException e) {
        } finally {
            systemManager.getTransactionManager().rollback();
        }

        factoryJE.closeConnection(0l);
        factoryJE2.closeConnection(0l);
        factoryJE.closeConnection(1l);
        factoryJE2.closeConnection(1l);
    }

    public void testFailLoad() throws ResourceException, SystemException, RollbackException {
        IQueuePersistenceController controller = (IQueuePersistenceController) ctx.getBean("bpqController");
        JEConnectionFactory factoryJE = (JEConnectionFactory) ctx.getBean("connectionFactory");
        ISystemManagerInternal systemManager = ctx.getBean(ISystemManagerInternal.class);
        systemManager.getTransactionManager().begin();
        Database database = factoryJE.getConnection(0l)
                .openDatabase("test_db", new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        database.put(null, new DatabaseEntry(SerializationHelper.longToBytes(11111)),
                new DatabaseEntry(SerializationHelper.longToBytes(11111)));
        systemManager.getTransactionManager().commit();

        systemManager.getTransactionManager().begin();
        try {
            controller.load(0);
            fail("Expected " + PersistenceQueueException.class);
        } catch (PersistenceQueueException e) {
        } finally {
            systemManager.getTransactionManager().rollback();
        }

        factoryJE.closeConnection(0l);
        factoryJE.closeConnection(1l);
    }

    public void testFailLoad_1() throws ResourceException, SystemException, RollbackException {
        IQueuePersistenceController controller = (IQueuePersistenceController) ctx.getBean("bpqController");
        JEConnectionFactory factoryJE = (JEConnectionFactory) ctx.getBean("connectionFactory");
        JEConnectionFactory factoryJE2 = (JEConnectionFactory) ctx.getBean("connectionFactory2");
        factoryJE2.getConnection(0l);
        ISystemManagerInternal systemManager = ctx.getBean(ISystemManagerInternal.class);
        systemManager.getTransactionManager().begin();

        try {
            controller.load(0);
            fail("Expected " + PersistenceQueueException.class);
        } catch (PersistenceQueueException e) {
        } finally {
            systemManager.getTransactionManager().rollback();
        }
        factoryJE.closeConnection(0l);
        factoryJE2.closeConnection(0l);
        factoryJE.closeConnection(1l);
        factoryJE2.closeConnection(1l);
    }

    public void testFailDelete() throws ResourceException, SystemException, RollbackException {
        IQueuePersistenceController controller = (IQueuePersistenceController) ctx.getBean("bpqController");
        JEConnectionFactory factoryJE = (JEConnectionFactory) ctx.getBean("connectionFactory");
        JEConnectionFactory factoryJE2 = (JEConnectionFactory) ctx.getBean("connectionFactory2");
        factoryJE2.getConnection(0l);
        ISystemManagerInternal systemManager = ctx.getBean(ISystemManagerInternal.class);
        systemManager.getTransactionManager().begin();

        try {
            controller.delete(Arrays.asList(new EventWithKey(new TestEvent("Test"))), 0);
            fail("Expected " + PersistenceQueueException.class);
        } catch (PersistenceQueueException e) {
        } finally {
            systemManager.getTransactionManager().rollback();
        }
        factoryJE.closeConnection(0l);
        factoryJE2.closeConnection(0l);
        factoryJE.closeConnection(1l);
        factoryJE2.closeConnection(1l);
    }

    public void testFailTransfer() throws ResourceException, SystemException, RollbackException {
        IQueuePersistenceController controller = (IQueuePersistenceController) ctx.getBean("bpqController");
        JEConnectionFactory factoryJE = (JEConnectionFactory) ctx.getBean("connectionFactory");
        JEConnectionFactory factoryJE2 = (JEConnectionFactory) ctx.getBean("connectionFactory2");
        factoryJE2.getConnection(0l);
        ISystemManagerInternal systemManager = ctx.getBean(ISystemManagerInternal.class);
        systemManager.getTransactionManager().begin();

        try {
            controller.transfer(1000, 0, 1);
            fail("Expected " + PersistenceQueueException.class);
        } catch (PersistenceQueueException e) {
        } finally {
            systemManager.getTransactionManager().rollback();
        }
        factoryJE.closeConnection(0l);
        factoryJE2.closeConnection(0l);
        factoryJE.closeConnection(1l);
        factoryJE2.closeConnection(1l);
    }

    public void testJEConnectionFail() {
        IQueuePersistenceController controller = (IQueuePersistenceController) ctx.getBean("bpqController");
        JEConnectionFactory factoryJE = (JEConnectionFactory) ctx.getBean("connectionFactory");
        ISystemManagerInternal systemManager = ctx.getBean(ISystemManagerInternal.class);
        try {
            factoryJE.getTxConnection(0l);
            fail("Expected " + JEConnectionException.class);
        } catch (JEConnectionException e) {
        }

        factoryJE.destroy();

    }

    public void testPersists_Rollback() throws ResourceException, SystemException, RollbackException {
        IQueuePersistenceController controller = (IQueuePersistenceController) ctx.getBean("bpqController");
        JEConnectionFactory factoryJE = (JEConnectionFactory) ctx.getBean("connectionFactory");
        ISystemManagerInternal systemManager = ctx.getBean(ISystemManagerInternal.class);
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
