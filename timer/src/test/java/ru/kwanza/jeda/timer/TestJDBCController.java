package ru.kwanza.jeda.timer;

import ru.kwanza.dbtool.DBTool;
import ru.kwanza.jeda.api.TimerItem;
import org.springframework.context.ApplicationContext;

import java.util.Collection;

public abstract class TestJDBCController extends TestController {

    @Override
    protected void persist(Collection<TimerItem> events) throws Exception {
        getController().persist(events);
    }

    @Override
    protected void delete(Collection<TimerItem> result) throws Exception {
        getController().delete(result);
    }

    @Override
    protected Collection<TimerItem> load(long size, long fromMillis) throws Exception {
        return getController().load(size, fromMillis);
    }

    @Override
    protected Collection<TimerItem> transfer(long count, long oldNodeId) throws Exception {
        return getController().transfer(count, oldNodeId);
    }

    @Override
    protected long size() throws Exception {
        return getController().getSize();
    }

    @Override
    protected void clean() throws Exception {
    }

    @Override
    protected void cleanForContext(ApplicationContext ctx) throws Exception {
        ctx.getBean("dbtool.DBTool", DBTool.class).getDataSource().getConnection().
                prepareStatement("delete from EVENT_TIMER").execute();
    }

    @Override
    protected void finish() {
    }
}
