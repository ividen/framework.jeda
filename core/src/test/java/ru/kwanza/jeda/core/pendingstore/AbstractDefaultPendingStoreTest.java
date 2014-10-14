package ru.kwanza.jeda.core.pendingstore;

import ru.kwanza.autokey.api.IAutoKey;
import ru.kwanza.autokey.mock.MockAutoKeyImpl;
import ru.kwanza.jeda.api.*;
import ru.kwanza.jeda.core.pendingstore.env.FlowBusBehaviour;
import ru.kwanza.jeda.core.pendingstore.env.FlowBusEventStore;
import ru.kwanza.jeda.core.pendingstore.env.TestEvent;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.EOFException;
import java.io.NotSerializableException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;

import static ru.kwanza.jeda.core.pendingstore.env.FlowBusBehaviour.SinkExceptionType.OTHER;
import static java.util.Arrays.asList;

public abstract class AbstractDefaultPendingStoreTest extends TestCase {

    protected static final String TEST_FLOW_BUS_1 = "TestFlowBus1";
    protected static final String TEST_FLOW_BUS_2 = "TestFlowBus2";
    protected static final String TEST_FLOW_BUS_3 = "TestFlowBus3";
    protected static final String PENDING_STORE_TABLE_NAME = "pending_store";

    protected ApplicationContext ctx;
    protected Connection conn;
    protected DBUnitUtil dbUnitUtil;
    protected IPendingStore pendingStore;
    protected IJedaManager manager;

    @Override
    public void setUp() throws Exception {
        ctx = new ClassPathXmlApplicationContext(getContextFileName(), AbstractDefaultPendingStoreTest.class);
        conn = ctx.getBean("dataSource", BasicDataSource.class).getConnection();
        manager = ctx.getBean(IJedaManager.class);
        dbUnitUtil = new DBUnitUtil(conn);
        pendingStore = manager.getPendingStore();
        conn.prepareStatement("DELETE FROM " + PENDING_STORE_TABLE_NAME).execute();
        resetAutoKey();
        FlowBusEventStore.clear();
        FlowBusBehaviour.reset();
    }

    @Override
    public void tearDown() throws Exception {
        ((ClassPathXmlApplicationContext) ctx).close();
    }

    protected abstract String getContextFileName();

    public void testSuspendEventBySinkName() throws Exception {
        ISuspender<TestEvent> suspender = pendingStore.getSuspender();
        Assert.assertEquals(getTestSuspendItem(1, TEST_FLOW_BUS_1), suspender.suspend(TEST_FLOW_BUS_1, createTestEvent(1, TEST_FLOW_BUS_1)));

        suspender.flush();
        dbUnitUtil.assertDBTable(PENDING_STORE_TABLE_NAME, "testSuspendEventBySinkName.xml");
    }

    public void testSuspendEventBySinkObject() throws Exception {
        ISuspender<TestEvent> suspender = pendingStore.getSuspender();
        @SuppressWarnings("unchecked") ISink<TestEvent> sink = manager.getFlowBus(TEST_FLOW_BUS_1);
        Assert.assertEquals(getTestSuspendItem(1, sink), suspender.suspend(sink, createTestEvent(1, sink)));

        suspender.flush();
        dbUnitUtil.assertDBTable(PENDING_STORE_TABLE_NAME, "testSuspendEventBySinkName.xml");
    }

    @SuppressWarnings("unchecked")
    public void testSuspendSeveralSeparateEvents() throws Exception {
        ISuspender<TestEvent> suspender = pendingStore.getSuspender();
        ISink<TestEvent> sink1 = manager.getFlowBus(TEST_FLOW_BUS_1);
        ISink<TestEvent> sink2 = manager.getFlowBus(TEST_FLOW_BUS_2);
        ISink<TestEvent> sink3 = manager.getFlowBus(TEST_FLOW_BUS_3);

        Assert.assertEquals(getTestSuspendItem(1, sink1), suspender.suspend(sink1, createTestEvent(1, sink1)));
        Assert.assertEquals(getTestSuspendItem(2, sink2), suspender.suspend(sink2, createTestEvent(2, sink2)));
        Assert.assertEquals(getTestSuspendItem(3, sink3), suspender.suspend(sink3, createTestEvent(3, sink3)));

        Assert.assertEquals(getTestSuspendItem(4, sink1), suspender.suspend(TEST_FLOW_BUS_1, createTestEvent(4, TEST_FLOW_BUS_1)));
        Assert.assertEquals(getTestSuspendItem(5, sink2), suspender.suspend(TEST_FLOW_BUS_2, createTestEvent(5, TEST_FLOW_BUS_2)));
        Assert.assertEquals(getTestSuspendItem(6, sink3), suspender.suspend(TEST_FLOW_BUS_3, createTestEvent(6, TEST_FLOW_BUS_3)));

        suspender.flush();
        dbUnitUtil.assertDBTable(PENDING_STORE_TABLE_NAME, "testSuspendSeveralSeparateEvents.xml");
    }

    public void testSuspendEventsBySinkName() throws Exception {
        ISuspender<TestEvent> suspender = pendingStore.getSuspender();

        Assert.assertEquals(getExpectedEventToIdMapping(TEST_FLOW_BUS_1, 1l),
                suspender.suspend(TEST_FLOW_BUS_1, getTestEventsBySinkName().get(TEST_FLOW_BUS_1)));

        Assert.assertEquals(getExpectedEventToIdMapping(TEST_FLOW_BUS_3, 4l),
                suspender.suspend(TEST_FLOW_BUS_3, getTestEventsBySinkName().get(TEST_FLOW_BUS_3)));

        suspender.flush();
        dbUnitUtil.assertDBTable(PENDING_STORE_TABLE_NAME, "testSuspendEventsBySinkName.xml");
    }

    public void testSuspendEventsBySinkObject() throws Exception {
        ISuspender<TestEvent> suspender = pendingStore.getSuspender();

        @SuppressWarnings("unchecked")
        ISink<TestEvent> sink1 = manager.getFlowBus(TEST_FLOW_BUS_1);
        Assert.assertEquals(getExpectedEventToIdMapping(TEST_FLOW_BUS_1, 1l),
                suspender.suspend(sink1, getTestEventsBySinkName().get(TEST_FLOW_BUS_1)));

        @SuppressWarnings("unchecked")
        ISink<TestEvent> sink3 = manager.getFlowBus(TEST_FLOW_BUS_3);
        Assert.assertEquals(getExpectedEventToIdMapping(TEST_FLOW_BUS_3, 4l),
                suspender.suspend(sink3, getTestEventsBySinkName().get(TEST_FLOW_BUS_3)));

        suspender.flush();
        dbUnitUtil.assertDBTable(PENDING_STORE_TABLE_NAME, "testSuspendEventsBySinkName.xml");
    }

    public void testSuspendEventConstrained() throws Exception {
        ISuspender<TestEvent> suspender = pendingStore.getSuspender();
        NullToStringEvent constrainedEvent = new NullToStringEvent(1, TEST_FLOW_BUS_1);
        suspender.suspend(TEST_FLOW_BUS_1, constrainedEvent);

        try {
            suspender.flush();
            TestCase.fail("Exception must be thrown.");
        } catch (SuspendException e) {
            Assert.assertEquals(1, e.getFailedToSuspendEvents().size());
            Assert.assertEquals(constrainedEvent, e.getFailedToSuspendEvents().get(0));
        }
    }

    public void testSuspendNotSerializable() throws Exception {
        ISuspender<TestEvent> suspender = pendingStore.getSuspender();
        suspender.suspend(TEST_FLOW_BUS_1, new TestEvent(1, TEST_FLOW_BUS_1) {
        });

        try {
            suspender.flush();
            TestCase.fail("Exception must be thrown.");
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof NotSerializableException);
            Assert.assertEquals("Couldn't serialize event", e.getMessage());
        }
    }

    public void testSimpleResume() throws Exception {
        ISuspender<TestEvent> suspender = pendingStore.getSuspender();

        TestEvent testEvent = createTestEvent(1, TEST_FLOW_BUS_1);
        suspender.suspend(TEST_FLOW_BUS_1, testEvent);
        suspender.flush();

        pendingStore.resume(asList(1l));
        List<TestEvent> actualEvents = FlowBusEventStore.getEvents(TEST_FLOW_BUS_1);
        Assert.assertEquals(1, actualEvents.size());
        Assert.assertEquals(testEvent, actualEvents.get(0));

        Assert.assertEquals(0, dbUnitUtil.getRowCount(PENDING_STORE_TABLE_NAME));
    }

    public void testResumeWithSeveralSinks() throws Exception {
        ISuspender<TestEvent> suspender = pendingStore.getSuspender();
        suspender.suspend(TEST_FLOW_BUS_1, getTestEventsBySinkName().get(TEST_FLOW_BUS_1));
        suspender.suspend(TEST_FLOW_BUS_2, getTestEventsBySinkName().get(TEST_FLOW_BUS_2));
        suspender.suspend(TEST_FLOW_BUS_3, getTestEventsBySinkName().get(TEST_FLOW_BUS_3));
        suspender.flush();

        pendingStore.resume(asList(1l, 4l, 7l));

        List<TestEvent> flowBus1Events = FlowBusEventStore.getEvents(TEST_FLOW_BUS_1);
        List<TestEvent> flowBus2Events = FlowBusEventStore.getEvents(TEST_FLOW_BUS_2);
        List<TestEvent> flowBus3Events = FlowBusEventStore.getEvents(TEST_FLOW_BUS_3);

        Assert.assertEquals(1, flowBus1Events.size());
        Assert.assertEquals(1, flowBus2Events.size());
        Assert.assertEquals(1, flowBus3Events.size());

        Assert.assertEquals(createTestEvent(1, TEST_FLOW_BUS_1), flowBus1Events.get(0));
        Assert.assertEquals(createTestEvent(1, TEST_FLOW_BUS_2), flowBus2Events.get(0));
        Assert.assertEquals(createTestEvent(1, TEST_FLOW_BUS_3), flowBus3Events.get(0));
    }

    public void testResumeWithSinkException() throws Exception {
        FlowBusBehaviour.setSinkExceptionType(OTHER);

        ISuspender<TestEvent> suspender = pendingStore.getSuspender();

        TestEvent testEvent = createTestEvent(1, TEST_FLOW_BUS_1);
        suspender.suspend(TEST_FLOW_BUS_1, testEvent);
        suspender.flush();

        try {
            pendingStore.resume(asList(1l));
            TestCase.fail("Exception must be thrown.");
        } catch (ResumeException e) {
            Assert.assertEquals(1, e.getUnableToResumeEventIds().size());
        }

        Assert.assertNull(FlowBusEventStore.getEvents(TEST_FLOW_BUS_1));
        dbUnitUtil.assertDBTable(PENDING_STORE_TABLE_NAME, "testResumeWithSinkException.xml");
    }

    public void testResumeWithDeserializeException() throws Exception {
        insertPendingStoreRecord(1l, "ABC");
        try {
            pendingStore.resume(asList(1l));
            TestCase.fail("Exception must be thrown.");
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof EOFException);
            Assert.assertEquals("Couldn't deserialize event", e.getMessage());
        }

        Assert.assertEquals(1, dbUnitUtil.getRowCount(PENDING_STORE_TABLE_NAME));
    }

    public void testTryResumeWithSeveralSinks() throws Exception {
        ISuspender<TestEvent> suspender = pendingStore.getSuspender();
        suspender.suspend(TEST_FLOW_BUS_1, getTestEventsBySinkName().get(TEST_FLOW_BUS_1));
        suspender.suspend(TEST_FLOW_BUS_2, getTestEventsBySinkName().get(TEST_FLOW_BUS_2));
        suspender.suspend(TEST_FLOW_BUS_3, getTestEventsBySinkName().get(TEST_FLOW_BUS_3));
        suspender.flush();

        pendingStore.tryResume(asList(1l, 4l, 7l));

        List<TestEvent> flowBus1Events = FlowBusEventStore.getEvents(TEST_FLOW_BUS_1);
        List<TestEvent> flowBus2Events = FlowBusEventStore.getEvents(TEST_FLOW_BUS_2);
        List<TestEvent> flowBus3Events = FlowBusEventStore.getEvents(TEST_FLOW_BUS_3);

        Assert.assertEquals(1, flowBus1Events.size());
        Assert.assertEquals(1, flowBus2Events.size());
        Assert.assertEquals(1, flowBus3Events.size());

        Assert.assertEquals(createTestEvent(1, TEST_FLOW_BUS_1), flowBus1Events.get(0));
        Assert.assertEquals(createTestEvent(1, TEST_FLOW_BUS_2), flowBus2Events.get(0));
        Assert.assertEquals(createTestEvent(1, TEST_FLOW_BUS_3), flowBus3Events.get(0));
    }

    public void testTryResumeWithRemaining() throws Exception {
        FlowBusBehaviour.setRemainTryPutCount(2);
        ISuspender<TestEvent> suspender = pendingStore.getSuspender();
        suspender.suspend(TEST_FLOW_BUS_1, getTestEventsBySinkName().get(TEST_FLOW_BUS_1));
        suspender.flush();

        try {
            pendingStore.tryResume(asList(1l, 2l, 3l));
            TestCase.fail("Exception must be thrown.");
        } catch (ResumeException e) {
            Collections.sort(e.getUnableToResumeEventIds());
            Assert.assertEquals(asList(2l, 3l), e.getUnableToResumeEventIds());
        }

        List<TestEvent> flowBus1Events = FlowBusEventStore.getEvents(TEST_FLOW_BUS_1);
        Assert.assertEquals(1, flowBus1Events.size());
        Assert.assertEquals(createTestEvent(1, TEST_FLOW_BUS_1), flowBus1Events.get(0));
    }

    public void testTryResumeWithException() throws Exception {
        FlowBusBehaviour.setSinkExceptionType(OTHER);

        ISuspender<TestEvent> suspender = pendingStore.getSuspender();

        TestEvent testEvent = createTestEvent(1, TEST_FLOW_BUS_1);
        suspender.suspend(TEST_FLOW_BUS_1, testEvent);
        suspender.flush();

        try {
            pendingStore.tryResume(asList(1l));
            TestCase.fail("Exception must be thrown.");
        } catch (ResumeException e) {
            Assert.assertEquals(1, e.getUnableToResumeEventIds().size());
        }

        Assert.assertNull(FlowBusEventStore.getEvents(TEST_FLOW_BUS_1));
        dbUnitUtil.assertDBTable(PENDING_STORE_TABLE_NAME, "testResumeWithSinkException.xml");
    }


    public void testRemove() throws Exception {
        ISuspender<TestEvent> suspender = pendingStore.getSuspender();
        suspender.suspend(TEST_FLOW_BUS_2, getTestEventsBySinkName().get(TEST_FLOW_BUS_2));
        suspender.suspend(TEST_FLOW_BUS_3, getTestEventsBySinkName().get(TEST_FLOW_BUS_3));
        suspender.flush();

        Assert.assertEquals(6, dbUnitUtil.getRowCount(PENDING_STORE_TABLE_NAME));
        pendingStore.remove(asList(1l, 4l, 5l));
        Assert.assertEquals(3, dbUnitUtil.getRowCount(PENDING_STORE_TABLE_NAME));
        dbUnitUtil.assertDBTable(PENDING_STORE_TABLE_NAME, "testRemove.xml");
    }

    public void testSetAttributesAnReferenceSafety() throws Exception {
        Suspender<IEvent> suspender = (Suspender<IEvent>) pendingStore.getSuspender();
        IEvent event = createTestEvent(1, TEST_FLOW_BUS_1);

        IEvent suspendedEvent = suspender.suspendEvents(TEST_FLOW_BUS_1, asList(event)).iterator().next();
        suspender.flush();

        Assert.assertTrue(event == suspendedEvent);
        Assert.assertEquals(Long.valueOf(1l), IPendingStore.SUSPEND_ID_ATTR.get(event));
        Assert.assertEquals(TEST_FLOW_BUS_1, IPendingStore.SUSPEND_SINK_NAME_ATTR.get(event));
    }

    private TestEvent getTestSuspendItem(int i, ISink<TestEvent> sink) {
        final TestEvent testEvent = createTestEvent(i, sink);
        IPendingStore.SUSPEND_ID_ATTR.set(testEvent,(long)i);
        IPendingStore.SUSPEND_SINK_NAME_ATTR.set(testEvent,manager.resolveObjectName(sink));
        return testEvent;
    }

    @SuppressWarnings("unchecked")
    private TestEvent getTestSuspendItem(int i, String sinkName) {
        return getTestSuspendItem(i, manager.getFlowBus(sinkName));
    }

    private Map<String, List<TestEvent>> getTestEventsBySinkName() {
        Map<String, List<TestEvent>> eventsBySinkName = new HashMap<String, List<TestEvent>>();
        addEvents(TEST_FLOW_BUS_1, eventsBySinkName);
        addEvents(TEST_FLOW_BUS_2, eventsBySinkName);
        addEvents(TEST_FLOW_BUS_3, eventsBySinkName);
        return eventsBySinkName;
    }

    private Collection<TestEvent> getExpectedEventToIdMapping(String sinkName, Long startId) {
        Collection<TestEvent> idByEvent = new LinkedList<TestEvent>();
        for (int i = 1; i <= 3; i++) {
            final TestEvent testEvent = createTestEvent(i, sinkName);
            IPendingStore.SUSPEND_ID_ATTR.set(testEvent,startId++);
            idByEvent.add(testEvent);
        }
        return idByEvent;
    }

    private <T> void addEvents(T sink, Map<T, List<TestEvent>> eventsBySinkName) {
        List<TestEvent> eventList = new ArrayList<TestEvent>();
        for (int i = 1; i <= 3; i++) {
            eventList.add(createTestEvent(i, sink));
        }
        eventsBySinkName.put(sink, eventList);
    }

    private <T> TestEvent createTestEvent(Integer number, T sink) {
        String postfix = sink instanceof String ? (String) sink : manager.resolveObjectName(sink);
        return new TestEvent(number, "testParam-" + postfix);
    }

    private void resetAutoKey() {
        IAutoKey autoKey = ctx.getBean("autokey.IAutoKey", IAutoKey.class);
        ((MockAutoKeyImpl) autoKey).resetSequences();
    }

    private void insertPendingStoreRecord(Long id, Object event) throws Exception {
        PreparedStatement pst =
                conn.prepareStatement("INSERT INTO " +
                        PENDING_STORE_TABLE_NAME +
                        " (id, sink_name, event_description, event_binary) VALUES(?, ?, ?, ?)");
        pst.setLong(1, id);
        pst.setString(2, "TestSinkX");
        pst.setString(3, event.toString());
        pst.setBytes(4, event.toString().getBytes());
        assertEquals("No update count!", false, pst.execute());
        assertEquals("Wrong update count!", 1, pst.getUpdateCount());
    }

    private static class NullToStringEvent extends TestEvent {

        public NullToStringEvent(Integer id, String param) {
            super(id, param);
        }

        @Override
        public String toString() {
            return null;
        }

    }

}
