package ru.kwanza.jeda.persistentqueue.berkeley;

import ru.kwanza.jeda.api.Manager;
import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.api.internal.ISystemManager;
import ru.kwanza.jeda.clusterservice.impl.mock.MockClusterServiceImpl;
import ru.kwanza.jeda.jeconnection.JEConnectionFactory;
import ru.kwanza.jeda.persistentqueue.IQueuePersistenceController;
import ru.kwanza.jeda.persistentqueue.PersistentQueue;
import junit.framework.TestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
        PersistentQueue queue = new PersistentQueue(ctx.getBean(ISystemManager.class.getName(),
                ISystemManager.class), 1000, (IQueuePersistenceController) ctx.getBean("bpqController"));
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();

        Manager.getTM().begin();
        try {
            queue.put(Arrays.asList(new TestEvent("TestContextController")));
            Manager.getTM().commit();
        } catch (SinkException e) {
            Manager.getTM().rollback();
        }

        MockClusterServiceImpl.getInstance().generateCurrentNodeLost();
        factoryJE.closeConnection(0l);

        queue = new PersistentQueue(ctx.getBean(ISystemManager.class.getName(),
                ISystemManager.class), 1000, (IQueuePersistenceController) ctx.getBean("bpqController"));

        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();


        Manager.getTM().begin();
        try {
            assertEquals(queue.take(10).size(), 1);
            Manager.getTM().commit();
        } catch (Exception e) {
            Manager.getTM().rollback();
            throw e;
        }

        Manager.getTM().begin();
        try {
            assertNull(queue.take(10));
            Manager.getTM().commit();
        } catch (Exception e) {
            Manager.getTM().rollback();
            throw e;
        }

        factoryJE.closeConnection(0l);
        factoryJE.closeConnection(01l);
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
