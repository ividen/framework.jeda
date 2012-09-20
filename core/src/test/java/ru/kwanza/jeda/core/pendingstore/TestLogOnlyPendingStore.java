package ru.kwanza.jeda.core.pendingstore;

import ru.kwanza.jeda.api.ISink;
import ru.kwanza.jeda.api.Manager;
import ru.kwanza.jeda.api.SuspendException;
import ru.kwanza.jeda.core.pendingstore.env.TestEvent;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Dmitry Zagorovsky
 */
public class TestLogOnlyPendingStore extends TestCase {

    private static final String CONTEXT_FILE_NAME = "pendingstore-logonly-config.xml";
    private static final String TEST_FLOW_BUS_1 = "TestFlowBus1";

    protected ApplicationContext ctx;
    protected LogOnlyPendingStore pendingStore;

    @Override
    public void setUp() throws Exception {
        ctx = new ClassPathXmlApplicationContext(CONTEXT_FILE_NAME, AbstractDefaultPendingStoreTest.class);
        pendingStore = (LogOnlyPendingStore) Manager.getPendingStore();
    }

    public void testGetSuspender() throws Exception {
        Assert.assertTrue(pendingStore.getSuspender() != null);
    }

    public void testPendingStoreResume() throws Exception {
        try {
            pendingStore.resume(new ArrayList<Long>());
            TestCase.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof UnsupportedOperationException);
            Assert.assertEquals("It is non-persistent pending store.", e.getMessage());
        }
    }

    public void testPendingStoreTryResume() throws Exception {
        try {
            pendingStore.tryResume(new ArrayList<Long>());
            TestCase.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof UnsupportedOperationException);
            Assert.assertEquals("It is non-persistent pending store.", e.getMessage());
        }
    }

    public void testPendingStoreRemove() throws Exception {
        try {
            pendingStore.remove(new ArrayList<Long>());
            TestCase.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof UnsupportedOperationException);
            Assert.assertEquals("It is non-persistent pending store.", e.getMessage());
        }
    }

    public void testSuspenderSuspendEventBySinkName() throws Exception {
        String sinkName1 = "sinkName1";
        String sinkName2 = "sinkName2";
        TestEvent testEvent1 = new TestEvent(1, "param1");
        TestEvent testEvent2 = new TestEvent(2, "param2");

        LogOnlySuspender<TestEvent> suspender = pendingStore.getSuspender();

        Assert.assertEquals(testEvent1, suspender.suspend(sinkName1, testEvent1));
        Assert.assertEquals(1, suspender.getSuspends().size());
        Assert.assertTrue(suspender.getSuspends().contains(testEvent1));

        Assert.assertEquals(testEvent2, suspender.suspend(sinkName2, testEvent2));
        Assert.assertEquals(2, suspender.getSuspends().size());
        Assert.assertTrue(suspender.getSuspends().contains(testEvent2));

        suspender.flush();
    }

    public void testSuspenderSuspendEventsBySinkName() throws Exception {
        String sinkName1 = "sinkName1";
        TestEvent testEvent1 = new TestEvent(1, "param1");
        TestEvent testEvent2 = new TestEvent(2, "param2");
        List<TestEvent> eventList = Arrays.asList(testEvent1, testEvent2);

        LogOnlySuspender<TestEvent> suspender = pendingStore.getSuspender();


        @SuppressWarnings("unchecked")
        List<TestEvent> expectedSuspendedItems = Arrays.asList(testEvent1,
                testEvent2);
        Assert.assertEquals(expectedSuspendedItems, suspender.suspend(sinkName1, eventList));

        Assert.assertEquals(expectedSuspendedItems, suspender.getSuspends());

        suspender.flush();
    }

    public void testSuspenderSuspendEventBySink() throws SuspendException {
        @SuppressWarnings("unchecked")
        ISink<TestEvent> sink = Manager.getFlowBus(TEST_FLOW_BUS_1);
        TestEvent testEvent1 = new TestEvent(1, "param1");

        LogOnlySuspender<TestEvent> suspender = pendingStore.getSuspender();

        Assert.assertEquals(testEvent1, suspender.suspend(sink, testEvent1));
        Assert.assertEquals(1, suspender.getSuspends().size());
        Assert.assertTrue(suspender.getSuspends().contains(testEvent1));

        suspender.flush();
    }

    public void testSuspenderSuspendEventsBySink() throws Exception {
        @SuppressWarnings("unchecked")
        ISink<TestEvent> sink = Manager.getFlowBus(TEST_FLOW_BUS_1);
        TestEvent testEvent1 = new TestEvent(1, "param1");
        TestEvent testEvent2 = new TestEvent(2, "param2");
        List<TestEvent> eventList = Arrays.asList(testEvent1, testEvent2);

        LogOnlySuspender<TestEvent> suspender = pendingStore.getSuspender();


        @SuppressWarnings("unchecked")
        List<TestEvent> expectedSuspendedItems = Arrays.asList(testEvent1, testEvent2);
        Assert.assertEquals(expectedSuspendedItems, suspender.suspend(sink, eventList));

        Assert.assertEquals(expectedSuspendedItems, suspender.getSuspends());

        suspender.flush();
    }

}
