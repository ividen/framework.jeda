package ru.kwanza.jeda.persistentqueue.old.berkeley;

import junit.framework.TestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.clusterservice.old.impl.mock.MockClusterServiceImpl;
import ru.kwanza.jeda.jeconnection.JEConnectionFactory;
import ru.kwanza.jeda.persistentqueue.old.IQueuePersistenceController;
import ru.kwanza.jeda.persistentqueue.old.PersistentQueue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Guzanov Alexander
 */
public abstract class TestBerkeleyPersistentQueue extends TestCase {

    @Override
    public void setUp() throws Exception {
        delete(new File("./target/berkeley_db"));
        MockClusterServiceImpl.getInstance().clear();
    }

    public void testLoad() throws Exception {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                getContextName(), TestBerkeleyPersistentQueue.class);
        JEConnectionFactory factoryJE = (JEConnectionFactory) ctx.getBean("connectionFactory");
        IJedaManager systemManager = ctx.getBean(IJedaManager.class);

        PersistentQueue queue = new PersistentQueue(systemManager, 1000, (IQueuePersistenceController) ctx.getBean("bpqController"));
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();

        systemManager.getTransactionManager().begin();
        try {
            queue.put(Arrays.asList(new TestEvent("TestContextController")));
            systemManager.getTransactionManager().commit();
        } catch (SinkException e) {
            systemManager.getTransactionManager().rollback();
        }

        MockClusterServiceImpl.getInstance().generateCurrentNodeLost();
        factoryJE.closeConnection(0);

        queue = new PersistentQueue(systemManager, 1000, (IQueuePersistenceController) ctx.getBean("bpqController"));

        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();


        systemManager.getTransactionManager().begin();
        try {
            assertEquals(queue.take(10).size(), 1);
            systemManager.getTransactionManager().commit();
        } catch (Exception e) {
            systemManager.getTransactionManager().rollback();
            throw e;
        }

        systemManager.getTransactionManager().begin();
        try {
            assertNull(queue.take(10));
            systemManager.getTransactionManager().commit();
        } catch (Exception e) {
            systemManager.getTransactionManager().rollback();
            throw e;
        }

        factoryJE.closeConnection(0);
        factoryJE.closeConnection(01);
        ctx.destroy();

    }

    protected abstract String getContextName();

    private void delete(File file) throws IOException {
        if (file.isDirectory()) {
            for (File item : file.listFiles()) {
                delete(item);
            }
        }
        file.delete();
    }
}
