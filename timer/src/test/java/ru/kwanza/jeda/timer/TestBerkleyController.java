package ru.kwanza.jeda.timer;

import ru.kwanza.jeda.api.TimerItem;
import ru.kwanza.jeda.api.internal.IJedaManagerInternal;
import ru.kwanza.jeda.jeconnection.JEConnectionFactory;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public abstract class TestBerkleyController extends TestController {

    private JEConnectionFactory factoryJE;
    private IJedaManagerInternal manager;


    @Override
    protected void persist(Collection<TimerItem> events) throws Exception {
        manager.getTransactionManager().begin();
        getController().persist(events);
        manager.getTransactionManager().commit();
    }

    @Override
    protected void delete(Collection<TimerItem> result) throws Exception {
        manager.getTransactionManager().begin();
        getController().delete(result);
        manager.getTransactionManager().commit();
    }

    @Override
    protected Collection<TimerItem> load(long size, long fromMillis) throws Exception {
        manager.getTransactionManager().begin();
        Collection<TimerItem> load = getController().load(size, fromMillis);
        manager.getTransactionManager().commit();
        return load;
    }

    @Override
    protected Collection<TimerItem> transfer(long count, long oldNodeId) throws Exception {
        manager.getTransactionManager().begin();
        Collection<TimerItem> transfer = getController().transfer(count, oldNodeId);
        manager.getTransactionManager().commit();
        return transfer;
    }

    @Override
    protected long size() throws Exception {
        manager.getTransactionManager().begin();
        long size = getController().getSize();
        manager.getTransactionManager().commit();
        return size;
    }

    @Override
    protected void clean() throws Exception {
        delete(new File("./target/berkeley_db"));
    }

    @Override
    protected void cleanForContext(ApplicationContext ctx) throws Exception {
    }

    @Override
    protected void finish() {
        factoryJE.destroy();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        factoryJE = getCtx().getBean("connectionFactory", JEConnectionFactory.class);
        manager = getCtx().getBean(IJedaManagerInternal.class);
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
