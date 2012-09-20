package ru.kwanza.jeda.timer;

import ru.kwanza.jeda.api.Manager;
import ru.kwanza.jeda.api.TimerItem;
import ru.kwanza.jeda.jeconnection.JEConnectionFactory;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * @autor Sergey Shurinov 06.03.12 13:23
 */
public abstract class TestBerkleyController extends TestController {

    private JEConnectionFactory factoryJE;


    @Override
    protected void persist(Collection<TimerItem> events) throws Exception {
        Manager.getTM().begin();
        getController().persist(events);
        Manager.getTM().commit();
    }

    @Override
    protected void delete(Collection<TimerItem> result) throws Exception {
        Manager.getTM().begin();
        getController().delete(result);
        Manager.getTM().commit();
    }

    @Override
    protected Collection<TimerItem> load(long size, long fromMillis) throws Exception {
        Manager.getTM().begin();
        Collection<TimerItem> load = getController().load(size, fromMillis);
        Manager.getTM().commit();
        return load;
    }

    @Override
    protected Collection<TimerItem> transfer(long count, long oldNodeId) throws Exception {
        Manager.getTM().begin();
        Collection<TimerItem> transfer = getController().transfer(count, oldNodeId);
        Manager.getTM().commit();
        return transfer;
    }

    @Override
    protected long size() throws Exception {
        Manager.getTM().begin();
        long size = getController().getSize();
        Manager.getTM().commit();
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
