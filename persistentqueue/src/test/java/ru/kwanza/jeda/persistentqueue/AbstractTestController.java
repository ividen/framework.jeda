package ru.kwanza.jeda.persistentqueue;

import ru.kwanza.autokey.api.IAutoKey;
import ru.kwanza.dbtool.DBTool;
import ru.kwanza.jeda.persistentqueue.jdbc.JDBCQueuePersistenceController;
import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Ivan Baluk
 */
public abstract class AbstractTestController extends TestCase {
    private ApplicationContext ctx;
    private IQueuePersistenceController queueController;
    private long nodeId = 1;
    private static int tmp = 1;
    private List<EventWithKey> evtList;

    @Override
    public void setUp() throws Exception {
        ctx = new ClassPathXmlApplicationContext(getContextFileName(), this.getClass());
        ctx.getBean("dbtool.DBTool", DBTool.class).getDataSource().getConnection().
                prepareStatement("delete from event_queue").execute();
        queueController = ctx.getBean("defaultQueueController", IQueuePersistenceController.class);
        evtList = createEvtList(100);
        queueController.persist(evtList, nodeId);
        queueController.persist(createEvtList(151), 2);
    }

    public void testLoad() throws Exception {
        assertEquals(100, queueController.load(nodeId).size());
        assertEquals(151, queueController.load(2).size());
        assertEquals(0, queueController.load(3).size());
    }

    public void testConfigTable() throws Exception {
        JDBCQueuePersistenceController pc1 = new JDBCQueuePersistenceController("name");
        pc1.setAutoKey(ctx.getBean("autokey.IAutoKey", IAutoKey.class));
        pc1.setDbTool(ctx.getBean(DBTool.class));
        ctx.getBean("dbtool.DBTool", DBTool.class).getDataSource().getConnection().
                prepareStatement("delete from event_queue").execute();
        pc1.setTableName("event_queue");
        pc1.setIdColumn("id");
        pc1.setEventColumn("data");
        pc1.setNodeIdColumn("node_id");
        pc1.setQueueNameColumn("queue_name");
        evtList = createEvtList(100);
        pc1.persist(evtList, nodeId);
        pc1.persist(createEvtList(151), 2);


        assertEquals(100, pc1.load(nodeId).size());
        assertEquals(151, pc1.load(2).size());
        assertEquals(0, pc1.load(3).size());
    }

    public void testDelete() throws Exception {
        List<EventWithKey> deletedEvt = new ArrayList<EventWithKey>();
        for (int i = 0; i < 38; i++) {
            deletedEvt.add(evtList.get(i));
        }
        queueController.delete(deletedEvt, nodeId);
        Collection<EventWithKey> loadList = queueController.load(nodeId);
        assertEquals(62, loadList.size());
        for (EventWithKey evt : deletedEvt) {
            assertFalse(loadList.contains(evt));
        }
    }

    public void testTransfer() throws Exception {
        Collection<EventWithKey> transferList = queueController.transfer(69, nodeId, 2);
        assertEquals(69, transferList.size());
        assertEquals(220, queueController.load(2).size());
        assertEquals(31, queueController.load(nodeId).size());
    }

//    public void testDuplicateKeyException() throws Exception {
//        JDBCQueuePersistenceController pc1 = new JDBCQueuePersistenceController("name");
//        pc1.setAutoKey(ctx.getBean("autoKey", IAutoKey.class));
//        pc1.setDbTool(ctx.getBean(DBTool.class));
//
//        boolean throwCheck = false;
//        ctx.getBean("autoKey", MockAutoKeyImpl.class).resetSequences();
//        try {
//            pc1.persist(createEvtList(10), nodeId);
//            ctx.getBean("autoKey", MockAutoKeyImpl.class).resetSequences();
//            queueController.persist(createEvtList(10), nodeId);
//        } catch (PersistenceQueueException e) {
//            throwCheck = true;
//        }
//        assertTrue(throwCheck);
//    }

//    public void testConstraint() throws Exception {
//        JDBCQueuePersistenceController pc1 = new JDBCQueuePersistenceController("name");
//        pc1.setAutoKey(ctx.getBean("autoKey", IAutoKey.class));
//        pc1.setDbTool(ctx.getBean(DBTool.class));
//
//        pc1.persist(createEvtList(10), nodeId);
//        ctx.getBean("autoKey", MockAutoKeyImpl.class).resetSequences();
//        boolean throwCheck = false;
//        try {
//            queueController.persist(new ArrayList<EventWithKey>(Arrays.asList(new EventWithKeyConstraint(919191l, new Event("id")))), nodeId);
//        } catch (PersistenceQueueException e) {
//            throwCheck = true;
//        }
//        assertTrue(throwCheck);
//    }

    private List<EventWithKey> createEvtList(int size) {
        List<EventWithKey> list = new ArrayList<EventWithKey>();
        for (int i = tmp; i < tmp + size; i++) {
            list.add(new EventWithKey(i, new Event("ContextId: " + i)));
        }
        tmp = tmp + size;
        return list;
    }

    protected abstract String getContextFileName();
}
