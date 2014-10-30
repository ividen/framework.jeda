package ru.kwanza.jeda.timer;

import ru.kwanza.jeda.api.TimerItem;
import ru.kwanza.jeda.clusterservice.old.impl.mock.MockClusterServiceImpl;
import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Collection;

public abstract class TestController extends TestCase {

    private ApplicationContext ctx;
    private ITimerPersistentController controller;
    private ArrayList<TimerItem> evtList;
    private MockClusterServiceImpl impl;
    private int count = 100;
    private long time;

    protected abstract String getContextFileName();

    protected abstract void persist(Collection<TimerItem> events) throws Exception;

    protected abstract void delete(Collection<TimerItem> result) throws Exception;

    protected abstract Collection<TimerItem> load(long size, long fromMillis) throws Exception;

    protected abstract Collection<TimerItem> transfer(long count, int oldNodeId) throws Exception;

    protected abstract long size() throws Exception;

    protected abstract void clean() throws Exception;

    protected abstract void cleanForContext(ApplicationContext ctx) throws Exception;

    protected abstract void finish();

    protected ITimerPersistentController getController() {
        return controller;
    }

    protected ApplicationContext getCtx() {
        return ctx;
    }

    protected void setNodeId(long nodeId) {
        impl.setNodeId(nodeId);
//        ClusterService.getInstance().setImpl(impl);
    }

    protected ArrayList<TimerItem> createEvtList(int count, long time) {
        ArrayList<TimerItem> list = new ArrayList<TimerItem>();
        for (int i = 0; i < count; i++) {
            list.add(new TimerItem(new Event("ContextId: " + i), time + count - i));
        }
        return list;
    }

    @Override
    public void setUp() throws Exception {
        clean();
        ctx = new ClassPathXmlApplicationContext(getContextFileName(), this.getClass());
        cleanForContext(ctx);
        controller = ctx.getBean("defaultTimerController", ITimerPersistentController.class);
        impl = MockClusterServiceImpl.getInstance();
        time = System.currentTimeMillis();
        evtList = createEvtList(count, time);
    }

    @Override
    public void tearDown() throws Exception {
        finish();
    }

    public void testPersist() throws Exception {
        setNodeId(1);
        persist(createEvtList(count, System.currentTimeMillis()));
        persist(createEvtList(count, System.currentTimeMillis()));
        assertEquals(2 * count, size());
    }

    public void testPersistEmpty() throws Exception {
        setNodeId(1);
        persist(new ArrayList<TimerItem>());
        assertEquals(0, size());
    }

    public void testDeleteAndPersist() throws Exception {
        setNodeId(1);
        ArrayList<TimerItem> events = createEvtList(count, System.currentTimeMillis());
        persist(events);
        assertEquals(count, size());
        ArrayList<TimerItem> deleteEvents = new ArrayList<TimerItem>();
        int countDelete = 35;
        for (int i = 0; i < countDelete; i++) {
            deleteEvents.add(events.get(i));
        }
        delete(deleteEvents);
        assertEquals(count - countDelete, size());
        events = createEvtList(count, System.currentTimeMillis());
        persist(events);
        assertEquals(2 * count - countDelete, size());
    }

    public void testDeleteEmpty() throws Exception {
        delete(new ArrayList<TimerItem>());
        assertEquals(0, size());
    }

    public void testDeleteAll() throws Exception {
        setNodeId(1);
        persist(evtList);
        assertEquals(count, size());
        delete(evtList);
        assertEquals(0, size());
    }

    public void testDelete() throws Exception {
        setNodeId(1);
        ArrayList<TimerItem> events = createEvtList(count, System.currentTimeMillis());
        persist(events);
        assertEquals(count, size());
        ArrayList<TimerItem> deleteEvents = new ArrayList<TimerItem>();
        int countDelete = 35;
        for (int i = 0; i < countDelete; i++) {
            deleteEvents.add(events.get(i));
        }
        delete(deleteEvents);
        assertEquals(count - countDelete, size());
    }

    public void testLoadRageOffsetTimeRightIn() throws Exception {
        setNodeId(1);
        ArrayList<TimerItem> events = createEvtList(count, time);
        persist(events);
        int countLoad = 25;
        int offsetTime = 20;
        Collection<TimerItem> items = load(countLoad, time + offsetTime);
        assertEquals(countLoad, items.size());
        for (TimerItem item : items) {
            assertTrue(item.getMillis() < time + offsetTime + countLoad);
            assertTrue(item.getMillis() >= time + offsetTime);
        }
    }

    public void testLoadRageOffsetTimeRight() throws Exception {
        setNodeId(1);
        ArrayList<TimerItem> events = createEvtList(count, time);
        persist(events);
        int countLoad = 50;
        int offsetTime = 60;
        Collection<TimerItem> items = load(countLoad, time + offsetTime);
        assertEquals(countLoad - (countLoad - (count - offsetTime)) + 1, items.size());
        for (TimerItem item : items) {
            assertTrue(item.getMillis() >= time + offsetTime);
        }
    }

    public void testLoadRageOffsetTimeLeft() throws Exception {
        setNodeId(1);
        ArrayList<TimerItem> events = createEvtList(count, time);
        persist(events);
        int countLoad = 25;
        int offsetTime = 20;
        Collection<TimerItem> items = load(countLoad, time - offsetTime);
        assertEquals(countLoad, items.size());
        for (TimerItem item : items) {
            assertTrue(item.getMillis() <= time + countLoad);
        }
    }

    public void testLoadAllOffsetTimeRight() throws Exception {
        setNodeId(1);
        ArrayList<TimerItem> events = createEvtList(count, time);
        persist(events);
        int offsetTime = 20;
        Collection<TimerItem> items = load(count, time + offsetTime);
        assertEquals(count - offsetTime + 1, items.size());
        for (TimerItem item : items) {
            assertTrue(item.getMillis() >= time + offsetTime);
        }
    }

    public void testLoadAllOffsetTimeLeft() throws Exception {
        setNodeId(1);
        ArrayList<TimerItem> events = createEvtList(count, time);
        persist(events);
        Collection<TimerItem> items = load(count, time - 10);
        assertEquals(count, items.size());
    }

    public void testLoadAll() throws Exception {
        setNodeId(1);
        ArrayList<TimerItem> events = createEvtList(count, time);
        persist(events);
        Collection<TimerItem> items = load(count, time);
        assertEquals(count, items.size());
    }

    public void testLoadAllCount() throws Exception {
        setNodeId(1);
        ArrayList<TimerItem> events = createEvtList(count, time);
        persist(events);
        Collection<TimerItem> items = load(2 * count, time);
        assertEquals(count, items.size());
    }

    public void testLoadZero() throws Exception {
        setNodeId(1);
        ArrayList<TimerItem> events = createEvtList(count, time);
        persist(events);
        Collection<TimerItem> items = load(0, time);
        assertEquals(0, items.size());
    }

    public void testLoadEmpty() throws Exception {
        setNodeId(1);
        Collection<TimerItem> items = load(count, time);
        assertEquals(0, items.size());
    }

//    public void testTransferCount() throws Exception {
//        setNodeId(2);
//        persist(evtList);
//        assertEquals(count, size());
//
//        setNodeId(1);
//        transfer(10, 2);
//
//        Collection<TimerItem> load = load(count, 0);
//        assertEquals(10, load.size());
//        for (TimerItem item : load) {
//            assertTrue(item.getMillis() <= time + 10);
//            assertTrue(item.getMillis() > time);
//        }
//
//        setNodeId(2);
//        load = load(count, 0);
//        assertEquals(count - 10, load.size());
//    }
//
//    public void testTransferAll() throws Exception {
//        setNodeId(0);
//        persist(evtList);
//        assertEquals(count, size());
//
//        setNodeId(1);
//        transfer(count, 0);
//
//        Collection<TimerItem> load = load(count, 0);
//        assertEquals(count, load.size());
//        setNodeId(0);
//        load = load(count, 0);
//        assertEquals(0, load.size());
//    }
//
//    public void testTransferAllCount() throws Exception {
//        setNodeId(2);
//        persist(evtList);
//        assertEquals(count, size());
//
//        setNodeId(1);
//        transfer(2 * count, 2);
//
//        Collection<TimerItem> load = load(count, 0);
//        assertEquals(count, load.size());
//        setNodeId(2);
//        load = load(count, 0);
//        assertEquals(0, load.size());
//    }
//
//    public void testTransferAllEmpty() throws Exception {
//        setNodeId(1);
//        transfer(2 * count, 2);
//
//        Collection<TimerItem> load = load(count, 0);
//        assertEquals(0, load.size());
//    }
//
//    public void testTransferAllZero() throws Exception {
//        setNodeId(2);
//        persist(evtList);
//        assertEquals(count, size());
//
//        setNodeId(1);
//        transfer(0, 2);
//
//        Collection<TimerItem> load = load(count, 0);
//        assertEquals(0, load.size());
//    }
}
