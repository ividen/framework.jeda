package ru.kwanza.jeda.persistentqueue;

import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.api.internal.IQueueObserver;
import ru.kwanza.jeda.api.ISystemManager;
import ru.kwanza.jeda.api.internal.ISystemManagerInternal;
import ru.kwanza.jeda.api.internal.SourceException;
import ru.kwanza.jeda.clusterservice.impl.mock.MockClusterServiceImpl;
import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Guzanov Alexander
 */
public abstract class TestPersistentQueue extends TestCase {
    protected ApplicationContext ctx;
    protected PersistentQueue queue;
    protected TestQueuePersistenceController controller;

    @Override
    public void setUp() throws Exception {
        ctx = new ClassPathXmlApplicationContext(getContextName(), TestPersistentQueue.class);
        MockClusterServiceImpl.getInstance().clear();
        MockClusterServiceImpl.getInstance().setNodeId(1l);
        TestDataStore.getInstance().clear();
        controller = new TestQueuePersistenceController();
        queue = createQeueue();
    }

    protected abstract String getContextName();

    protected PersistentQueue createQeueue() {
        return new PersistentQueue((ISystemManagerInternal) ctx.getBean("ru.kwanza.jeda.api.ISystemManager"),
                1000, controller);
    }

    public void testActive() throws SinkException {
        try {
            queue.put(Collections.singleton(createEvent("Test")));
            fail("Expected " + SinkException.Closed.class);
        } catch (SinkException.Closed e) {

        }

        try {
            queue.tryPut(Collections.singleton(createEvent("Test")));
            fail("Expected " + SinkException.Closed.class);
        } catch (SinkException.Closed e) {

        }

        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());
        assertFalse(queue.isActive());
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertTrue(queue.isActive());
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());

        MockClusterServiceImpl.getInstance().generateCurrentNodeLost();
        assertFalse(queue.isActive());
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());
    }

    protected Event createEvent(String contextId) {
        return new Event(contextId);
    }

    public void testNoTx() throws SinkException {
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());
        assertFalse(queue.isActive());
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();


        try {
            queue.put(Collections.singleton(createEvent("Test")));
            fail("Expected " + SinkException.class);
        } catch (SinkException e) {
            assertEquals(e.getClass(), SinkException.class);
        }

        try {
            queue.tryPut(Collections.singleton(createEvent("Test")));
            fail("Expected " + SinkException.class);
        } catch (SinkException e) {
            assertEquals(e.getClass(), SinkException.class);
        }


        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());
        assertTrue(queue.isActive());
    }

    public void testPut_Interrupted() throws Throwable {
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());
        assertTrue(queue.isActive());

        Thread.currentThread().interrupt();
        queue.getManager().getTransactionManager().begin();
        try {
            try {
                queue.put(Collections.singleton(createEvent("Test1")));
                fail("Expected " + SinkException.Closed.class);
            } catch (SinkException.Closed e) {
            }
            queue.getManager().getTransactionManager().commit();
        } catch (Throwable e) {
            queue.getManager().getTransactionManager().rollback();
            throw e;
        }
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong estimate size", 0, queue.getEstimatedCount());
    }

    public void testTryPut_Interrupted() throws Throwable {
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());
        assertTrue(queue.isActive());

        Thread.currentThread().interrupt();
        queue.getManager().getTransactionManager().begin();
        try {
            try {
                queue.tryPut(Collections.singleton(createEvent("Test1")));
                fail("Expected " + SinkException.Closed.class);
            } catch (SinkException.Closed e) {
            }
            queue.getManager().getTransactionManager().commit();
        } catch (Throwable e) {
            queue.getManager().getTransactionManager().rollback();
            throw e;
        }
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong estimate size", 0, queue.getEstimatedCount());
    }

    public void testPut_Commit() throws SinkException {
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());
        assertTrue(queue.isActive());

        queue.getManager().getTransactionManager().begin();
        queue.put(Collections.singleton(createEvent("Test1")));
        queue.put(Arrays.asList(createEvent("Test2"), createEvent("Test3"), createEvent("Test4")));
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong estimate size", 4, queue.getEstimatedCount());
        queue.getManager().getTransactionManager().commit();
        assertEquals("Wrong size", 4, queue.size());
        assertEquals("Wrong estimate size", 4, queue.getEstimatedCount());

        Collection<Event> events = TestDataStore.getInstance().getEvents(1);
        assertTrue(exists(events, "Test1"));
        assertTrue(exists(events, "Test2"));
        assertTrue(exists(events, "Test3"));
        assertTrue(exists(events, "Test4"));
        MockClusterServiceImpl.getInstance().generateCurrentNodeLost();
        assertFalse(queue.isActive());
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());
    }

    public void testPut_Rollback() throws SinkException {
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());
        assertTrue(queue.isActive());

        queue.getManager().getTransactionManager().begin();
        queue.put(Collections.singleton(createEvent("Test1")));
        queue.put(Arrays.asList(createEvent("Test2"), createEvent("Test3"), createEvent("Test4")));
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong estimate size", 4, queue.getEstimatedCount());
        queue.getManager().getTransactionManager().rollback();
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong estimate size", 0, queue.getEstimatedCount());

        Collection<Event> events = TestDataStore.getInstance().getEvents(1);
        assertTrue(!exists(events, "Test1"));
        assertTrue(!exists(events, "Test2"));
        assertTrue(!exists(events, "Test3"));
        assertTrue(!exists(events, "Test4"));
        MockClusterServiceImpl.getInstance().generateCurrentNodeLost();
    }

    public void testPut_ErrorOnPersist() throws SinkException {
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());
        assertTrue(queue.isActive());

        controller.setErrorOnPersist(true);
        queue.getManager().getTransactionManager().begin();
        try {
            queue.put(Arrays.asList(createEvent("Test2"), createEvent("Test3"), createEvent("Test4")));
        } catch (Throwable e) {
            queue.getManager().getTransactionManager().rollback();
        }

        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong estimate size", 0, queue.getEstimatedCount());

        Collection<Event> events = TestDataStore.getInstance().getEvents(1);
        assertTrue(!exists(events, "Test1"));
        assertTrue(!exists(events, "Test2"));
        assertTrue(!exists(events, "Test3"));
        assertTrue(!exists(events, "Test4"));
    }

    public void testPut_Clogged() throws SinkException {
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());
        assertTrue(queue.isActive());

        queue.getManager().getTransactionManager().begin();
        ArrayList<Event> events = new ArrayList<Event>();
        for (int i = 0; i < 1001; i++) {
            events.add(createEvent("Test" + i));
        }
        try {
            queue.put(events);
            fail("Expected " + SinkException.Clogged.class);
        } catch (SinkException.Clogged e) {
        }

        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong estimate size", 0, queue.getEstimatedCount());
        queue.getManager().getTransactionManager().commit();
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong estimate size", 0, queue.getEstimatedCount());

        Collection<Event> storedEvents = TestDataStore.getInstance().getEvents(1);
        assertNull(storedEvents);
    }

    public void testTryPut_Commit() throws SinkException {
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());
        assertTrue(queue.isActive());

        queue.getManager().getTransactionManager().begin();
        assertNull(queue.tryPut(Collections.singleton(createEvent("Test1"))));
        assertNull(queue.tryPut(Arrays.asList(createEvent("Test2"), createEvent("Test3"), createEvent("Test4"))));
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong estimate size", 4, queue.getEstimatedCount());
        queue.getManager().getTransactionManager().commit();
        assertEquals("Wrong size", 4, queue.size());
        assertEquals("Wrong estimate size", 4, queue.getEstimatedCount());

        Collection<Event> events = TestDataStore.getInstance().getEvents(1);
        assertTrue(exists(events, "Test1"));
        assertTrue(exists(events, "Test2"));
        assertTrue(exists(events, "Test3"));
        assertTrue(exists(events, "Test4"));
        MockClusterServiceImpl.getInstance().generateCurrentNodeLost();
        assertFalse(queue.isActive());
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());
    }

    public void testTryPut_Rollback() throws SinkException {
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());
        assertTrue(queue.isActive());

        queue.getManager().getTransactionManager().begin();
        assertNull(queue.tryPut(Collections.singleton(createEvent("Test1"))));
        assertNull(queue.tryPut(Arrays.asList(createEvent("Test2"), createEvent("Test3"), createEvent("Test4"))));
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong estimate size", 4, queue.getEstimatedCount());
        queue.getManager().getTransactionManager().rollback();
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong estimate size", 0, queue.getEstimatedCount());

        Collection<Event> events = TestDataStore.getInstance().getEvents(1);
        assertTrue(!exists(events, "Test1"));
        assertTrue(!exists(events, "Test2"));
        assertTrue(!exists(events, "Test3"));
        assertTrue(!exists(events, "Test4"));
        MockClusterServiceImpl.getInstance().generateCurrentNodeLost();
    }

    public void testTryPut_Clogged() throws SinkException {
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());
        assertTrue(queue.isActive());

        queue.getManager().getTransactionManager().begin();
        ArrayList<Event> events = new ArrayList<Event>();
        for (int i = 0; i < 1001; i++) {
            events.add(createEvent("Test" + i));
        }

        Collection<Event> collection = queue.tryPut(events);

        assertEquals("Wrong decline size", 1, collection.size());
        assertEquals("Wrong element", "Test1000", collection.iterator().next().getContextId());

        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong estimate size", 1000, queue.getEstimatedCount());
        queue.getManager().getTransactionManager().commit();
        assertEquals("Wrong size", 1000, queue.size());
        assertEquals("Wrong estimate size", 1000, queue.getEstimatedCount());

        Collection<Event> storedEvents = TestDataStore.getInstance().getEvents(1);
        assertEquals("Wrong decline size", 1000, storedEvents.size());
        for (int i = 0; i < 1000; i++) {
            assertTrue(exists(storedEvents, "Test" + i));
        }
    }

    public void testReload() throws SinkException {
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());
        assertTrue(queue.isActive());
        queue.getManager().getTransactionManager().begin();
        queue.put(Collections.singleton(createEvent("Test1")));
        queue.put(Arrays.asList(createEvent("Test2"), createEvent("Test3"), createEvent("Test4")));
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong estimate size", 4, queue.getEstimatedCount());
        queue.getManager().getTransactionManager().commit();
        assertEquals("Wrong size", 4, queue.size());
        assertEquals("Wrong estimate size", 4, queue.getEstimatedCount());

        Collection<Event> events = TestDataStore.getInstance().getEvents(1);
        assertTrue(exists(events, "Test1"));
        assertTrue(exists(events, "Test2"));
        assertTrue(exists(events, "Test3"));
        assertTrue(exists(events, "Test4"));
        MockClusterServiceImpl.getInstance().generateCurrentNodeLost();
        assertFalse(queue.isActive());
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());

        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 4, queue.size());
        assertEquals("Wrong estimate size", 4, queue.getEstimatedCount());
    }

    public void testLoadEmpty() throws SinkException {
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());
        assertTrue(queue.isActive());
        TestDataStore.getInstance().add(1l, new ArrayList<EventWithKey>());
        MockClusterServiceImpl.getInstance().generateCurrentNodeLost();
        assertFalse(queue.isActive());
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());

        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong estimate size", 0, queue.getEstimatedCount());
    }


    public void testReloadWithError() throws SinkException {
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());
        assertTrue(queue.isActive());
        queue.getManager().getTransactionManager().begin();
        queue.put(Collections.singleton(createEvent("Test1")));
        queue.put(Arrays.asList(createEvent("Test2"), createEvent("Test3"), createEvent("Test4")));
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong estimate size", 4, queue.getEstimatedCount());
        queue.getManager().getTransactionManager().commit();
        assertEquals("Wrong size", 4, queue.size());
        assertEquals("Wrong estimate size", 4, queue.getEstimatedCount());

        Collection<Event> events = TestDataStore.getInstance().getEvents(1);
        assertTrue(exists(events, "Test1"));
        assertTrue(exists(events, "Test2"));
        assertTrue(exists(events, "Test3"));
        assertTrue(exists(events, "Test4"));
        MockClusterServiceImpl.getInstance().generateCurrentNodeLost();
        assertFalse(queue.isActive());
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());

        controller.setErrorONLoad(true);
        try {
            MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
            fail("Expected " + RuntimeException.class);
        } catch (RuntimeException e) {

        }
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong estimate size", 0, queue.getEstimatedCount());
    }


    public void testTake_commit() throws SinkException, SourceException {
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());
        assertTrue(queue.isActive());
        queue.getManager().getTransactionManager().begin();
        queue.put(Collections.singleton(createEvent("Test1")));
        queue.put(Arrays.asList(createEvent("Test2"), createEvent("Test3"), createEvent("Test4")));
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong estimate size", 4, queue.getEstimatedCount());
        queue.getManager().getTransactionManager().commit();
        Collection<Event> events = TestDataStore.getInstance().getEvents(1);
        assertTrue(exists(events, "Test1"));
        assertTrue(exists(events, "Test2"));
        assertTrue(exists(events, "Test3"));
        assertTrue(exists(events, "Test4"));

        queue.getManager().getTransactionManager().begin();
        Collection<Event> take = queue.take(1000);
        assertEquals("Wrong Size", 4, take.size());
        queue.getManager().getTransactionManager().commit();


        queue.getManager().getTransactionManager().begin();
        take = queue.take(1000);
        assertNull(take);
        queue.getManager().getTransactionManager().commit();

        Collection<Event> storedevents = TestDataStore.getInstance().getEvents(1);
        assertEquals("Must be empty", 0, storedevents.size());
        assertTrue(!exists(events, "Test1"));
        assertTrue(!exists(events, "Test2"));
        assertTrue(!exists(events, "Test3"));
        assertTrue(!exists(events, "Test4"));
    }

    public void testTake_rollback() throws SinkException, SourceException {
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());
        assertTrue(queue.isActive());
        queue.getManager().getTransactionManager().begin();
        queue.put(Collections.singleton(createEvent("Test1")));
        queue.put(Arrays.asList(createEvent("Test2"), createEvent("Test3"), createEvent("Test4")));
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong estimate size", 4, queue.getEstimatedCount());
        queue.getManager().getTransactionManager().commit();
        Collection<Event> events = TestDataStore.getInstance().getEvents(1);
        assertTrue(exists(events, "Test1"));
        assertTrue(exists(events, "Test2"));
        assertTrue(exists(events, "Test3"));
        assertTrue(exists(events, "Test4"));

        queue.getManager().getTransactionManager().begin();
        Collection<Event> take = queue.take(1000);
        assertEquals("Wrong Size", 4, take.size());
        queue.getManager().getTransactionManager().rollback();

        Collection<Event> storedevents = TestDataStore.getInstance().getEvents(1);
        assertEquals("Must be empty", 4, storedevents.size());
        assertTrue(exists(events, "Test1"));
        assertTrue(exists(events, "Test2"));
        assertTrue(exists(events, "Test3"));
        assertTrue(exists(events, "Test4"));


        queue.getManager().getTransactionManager().begin();
        take = queue.take(1000);
        assertEquals("Wrong Size", 4, take.size());
        queue.getManager().getTransactionManager().commit();

        storedevents = TestDataStore.getInstance().getEvents(1);
        assertEquals("Must be empty", 0, storedevents.size());
        assertTrue(!exists(events, "Test1"));
        assertTrue(!exists(events, "Test2"));
        assertTrue(!exists(events, "Test3"));
        assertTrue(!exists(events, "Test4"));
    }

    public void testTake_Active_passive() throws SinkException, SourceException {
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());
        assertTrue(queue.isActive());
        queue.getManager().getTransactionManager().begin();
        queue.put(Collections.singleton(createEvent("Test1")));
        queue.put(Arrays.asList(createEvent("Test2"), createEvent("Test3"), createEvent("Test4")));
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong estimate size", 4, queue.getEstimatedCount());
        queue.getManager().getTransactionManager().commit();
        Collection<Event> events = TestDataStore.getInstance().getEvents(1);
        assertTrue(exists(events, "Test1"));
        assertTrue(exists(events, "Test2"));
        assertTrue(exists(events, "Test3"));
        assertTrue(exists(events, "Test4"));

        MockClusterServiceImpl.getInstance().generateCurrentNodeLost();

        queue.getManager().getTransactionManager().begin();
        assertNull(queue.take(1000));
        queue.getManager().getTransactionManager().commit();

        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();

        queue.getManager().getTransactionManager().begin();
        Collection<Event> take = queue.take(1000);
        assertEquals("Wrong Size", 4, take.size());
        queue.getManager().getTransactionManager().commit();

        Collection<Event> storedevents = TestDataStore.getInstance().getEvents(1);
        assertEquals("Must be empty", 0, storedevents.size());
        assertTrue(!exists(events, "Test1"));
        assertTrue(!exists(events, "Test2"));
        assertTrue(!exists(events, "Test3"));
        assertTrue(!exists(events, "Test4"));
    }

    public void testTake_noTX() throws SinkException, SourceException {
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());
        assertTrue(queue.isActive());
        queue.getManager().getTransactionManager().begin();
        queue.put(Collections.singleton(createEvent("Test1")));
        queue.put(Arrays.asList(createEvent("Test2"), createEvent("Test3"), createEvent("Test4")));
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong estimate size", 4, queue.getEstimatedCount());
        queue.getManager().getTransactionManager().commit();
        Collection<Event> events = TestDataStore.getInstance().getEvents(1);
        assertTrue(exists(events, "Test1"));
        assertTrue(exists(events, "Test2"));
        assertTrue(exists(events, "Test3"));
        assertTrue(exists(events, "Test4"));

        try {
            assertNull(queue.take(1000));
            fail("Expected " + SourceException.class);
        } catch (SourceException e) {
        }

        queue.getManager().getTransactionManager().begin();
        Collection<Event> take = queue.take(1000);
        assertEquals("Wrong Size", 4, take.size());
        queue.getManager().getTransactionManager().commit();

        Collection<Event> storedevents = TestDataStore.getInstance().getEvents(1);
        assertEquals("Must be empty", 0, storedevents.size());
        assertTrue(!exists(events, "Test1"));
        assertTrue(!exists(events, "Test2"));
        assertTrue(!exists(events, "Test3"));
        assertTrue(!exists(events, "Test4"));
    }

    public void testTake_interrupt() throws SinkException, SourceException {
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());
        assertTrue(queue.isActive());
        queue.getManager().getTransactionManager().begin();
        queue.put(Collections.singleton(createEvent("Test1")));
        queue.put(Arrays.asList(createEvent("Test2"), createEvent("Test3"), createEvent("Test4")));
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong estimate size", 4, queue.getEstimatedCount());
        queue.getManager().getTransactionManager().commit();
        Collection<Event> events = TestDataStore.getInstance().getEvents(1);
        assertTrue(exists(events, "Test1"));
        assertTrue(exists(events, "Test2"));
        assertTrue(exists(events, "Test3"));
        assertTrue(exists(events, "Test4"));

        Thread.currentThread().interrupt();
        queue.getManager().getTransactionManager().begin();
        try {
            queue.take(1000);
            fail("Expected " + SourceException.class);
        } catch (SourceException e) {
        }
        queue.getManager().getTransactionManager().commit();

    }


    public static final class TestObserver implements IQueueObserver {
        long queueSize = 0;
        long delta = 0;

        public void notifyChange(long queueSize, long delta) {
            this.queueSize = queueSize;
            this.delta = delta;
        }
    }


    public void testObserver() throws SinkException, SourceException {
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong size", 0, queue.getEstimatedCount());
        assertTrue(queue.isActive());

        assertNull(queue.getObserver());
        queue.setObserver(new TestObserver());
        assertNotNull(queue.getObserver());
        TestObserver observer = new TestObserver();
        queue.setObserver(observer);

        assertEquals(observer, queue.getObserver());

        queue.getManager().getTransactionManager().begin();
        queue.put(Collections.singleton(createEvent("Test1")));
        queue.put(Arrays.asList(createEvent("Test2"), createEvent("Test3"), createEvent("Test4")));
        assertEquals("Wrong size", 0, queue.size());
        assertEquals("Wrong estimate size", 4, queue.getEstimatedCount());
        queue.getManager().getTransactionManager().commit();
        Collection<Event> events = TestDataStore.getInstance().getEvents(1);
        assertTrue(exists(events, "Test1"));
        assertTrue(exists(events, "Test2"));
        assertTrue(exists(events, "Test3"));
        assertTrue(exists(events, "Test4"));


        assertEquals("Notify size ", 4, observer.queueSize);
        assertEquals("Notify delta ", 4, observer.delta);
    }

    public void testNodeLost_1() throws SinkException, SourceException, InterruptedException {

        TestDataStore.getInstance().add(1, Arrays.asList(
                new EventWithKey(createEvent("1")),
                new EventWithKey(createEvent("2")),
                new EventWithKey(createEvent("3"))));
        TestDataStore.getInstance().add(2, Arrays.asList(
                new EventWithKey(createEvent("4")),
                new EventWithKey(createEvent("5")),
                new EventWithKey(createEvent("6"))));
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 3, queue.size());
        assertEquals("Wrong size", 3, queue.getEstimatedCount());
        assertTrue(queue.isActive());

        MockClusterServiceImpl.getInstance().generateNodeLost(2, System.currentTimeMillis());

        Thread.currentThread().sleep(1000);

        Collection<Event> events = TestDataStore.getInstance().getEvents(1);
        assertTrue(exists(events, "1"));
        assertTrue(exists(events, "2"));
        assertTrue(exists(events, "3"));
        assertTrue(exists(events, "4"));
        assertTrue(exists(events, "5"));
        assertTrue(exists(events, "6"));
        assertEquals(events.size(), 6);

        //никак не влияет на обработку
        MockClusterServiceImpl.getInstance().generateNodeActivate(2, System.currentTimeMillis());

        events = TestDataStore.getInstance().getEvents(2);
        assertEquals(events.size(), 0);

        queue.getManager().getTransactionManager().begin();
        Collection<Event> take = queue.take(1000);
        queue.getManager().getTransactionManager().commit();

        events = TestDataStore.getInstance().getEvents(1);
        assertTrue(!exists(events, "1"));
        assertTrue(!exists(events, "2"));
        assertTrue(!exists(events, "3"));
        assertTrue(!exists(events, "4"));
        assertTrue(!exists(events, "5"));
        assertTrue(!exists(events, "6"));
        assertEquals(events.size(), 0);
    }

    public void testNodeLost_2() throws SinkException, SourceException, InterruptedException {

        TestDataStore.getInstance().add(1, Arrays.asList(
                new EventWithKey(createEvent("1")),
                new EventWithKey(createEvent("2")),
                new EventWithKey(createEvent("3"))));
        TestDataStore.getInstance().add(2, Arrays.asList(
                new EventWithKey(createEvent("4")),
                new EventWithKey(createEvent("5")),
                new EventWithKey(createEvent("6"))));
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 3, queue.size());
        assertEquals("Wrong size", 3, queue.getEstimatedCount());
        assertTrue(queue.isActive());
        QueueEventsTransfer.getInstance().setMaxThreadCount(2);
        assertEquals(2, QueueEventsTransfer.getInstance().getMaxThreadCount());

        MockClusterServiceImpl.getInstance().generateNodeLost(2, System.currentTimeMillis());
        MockClusterServiceImpl.getInstance().generateNodeLost(2, System.currentTimeMillis());
        MockClusterServiceImpl.getInstance().generateNodeLost(2, System.currentTimeMillis());
        MockClusterServiceImpl.getInstance().generateNodeLost(2, System.currentTimeMillis());
        MockClusterServiceImpl.getInstance().generateNodeLost(2, System.currentTimeMillis());

        Thread.currentThread().sleep(1000);

        Collection<Event> events = TestDataStore.getInstance().getEvents(1);
        assertTrue(exists(events, "1"));
        assertTrue(exists(events, "2"));
        assertTrue(exists(events, "3"));
        assertTrue(exists(events, "4"));
        assertTrue(exists(events, "5"));
        assertTrue(exists(events, "6"));
        assertEquals(events.size(), 6);

        events = TestDataStore.getInstance().getEvents(2);
        assertEquals(events.size(), 0);

        queue.getManager().getTransactionManager().begin();
        Collection<Event> take = queue.take(1000);
        assertTrue(exists(take, "1"));
        assertTrue(exists(take, "2"));
        assertTrue(exists(take, "3"));
        assertTrue(exists(take, "4"));
        assertTrue(exists(take, "5"));
        assertTrue(exists(take, "6"));
        queue.getManager().getTransactionManager().commit();

        events = TestDataStore.getInstance().getEvents(1);
        assertTrue(!exists(events, "1"));
        assertTrue(!exists(events, "2"));
        assertTrue(!exists(events, "3"));
        assertTrue(!exists(events, "4"));
        assertTrue(!exists(events, "5"));
        assertTrue(!exists(events, "6"));
        assertEquals(events.size(), 0);
    }

    public void testNodeLost_3() throws SinkException, SourceException, InterruptedException {
        ArrayList<EventWithKey> list1 = new ArrayList<EventWithKey>();
        ArrayList<EventWithKey> list2 = new ArrayList<EventWithKey>();
        for (int i = 0; i < 1000; i++) {
            list1.add(new EventWithKey(createEvent(String.valueOf(i))));
            list2.add(new EventWithKey(createEvent(String.valueOf(1000 + i))));
        }
        TestDataStore.getInstance().add(1, list1);
        TestDataStore.getInstance().add(2, list2);

        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 1000, queue.size());
        assertEquals("Wrong size", 1000, queue.getEstimatedCount());
        assertTrue(queue.isActive());
        QueueEventsTransfer.getInstance().setMaxThreadCount(1);
        assertEquals(1, QueueEventsTransfer.getInstance().getMaxThreadCount());
        MockClusterServiceImpl.getInstance().generateNodeLost(2, System.currentTimeMillis());
        Thread.currentThread().sleep(1000);

        Collection<Event> events = TestDataStore.getInstance().getEvents(1);
        for (int i = 0; i < 1000; i++) {
            assertTrue(exists(events, String.valueOf(i)));
        }
        assertEquals(events.size(), 1000);

        events = TestDataStore.getInstance().getEvents(2);
        assertEquals(events.size(), 1000);

        for (int i = 0; i < 1000; i++) {
            queue.getManager().getTransactionManager().begin();
            Collection<Event> take = queue.take(1);
            queue.getManager().getTransactionManager().commit();
        }

        Thread.sleep(1000);
        events = TestDataStore.getInstance().getEvents(1);
        for (int i = 0; i < 1000; i++) {
            assertTrue(exists(events, String.valueOf(1000 + i)));
        }
        assertEquals(events.size(), 1000);
        events = TestDataStore.getInstance().getEvents(2);
        assertEquals(events.size(), 0);

        queue.getManager().getTransactionManager().begin();
        Collection<Event> take = queue.take(10000);
        queue.getManager().getTransactionManager().commit();

    }

    public void testNodeLost_4() throws SinkException, SourceException, InterruptedException {
        ArrayList<EventWithKey> list1 = new ArrayList<EventWithKey>();
        ArrayList<EventWithKey> list2 = new ArrayList<EventWithKey>();
        ArrayList<EventWithKey> list3 = new ArrayList<EventWithKey>();
        for (int i = 0; i < 1000; i++) {
            list1.add(new EventWithKey(createEvent(String.valueOf(i))));
        }

        for (int i = 0; i < 500; i++) {
            list2.add(new EventWithKey(createEvent(String.valueOf(1000 + i))));
            list3.add(new EventWithKey(createEvent(String.valueOf(1500 + i))));
        }
        TestDataStore.getInstance().add(1, list1);
        TestDataStore.getInstance().add(2, list2);
        TestDataStore.getInstance().add(3, list3);
        QueueEventsTransfer.getInstance().setMaxThreadCount(2);
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 1000, queue.size());
        assertEquals("Wrong size", 1000, queue.getEstimatedCount());
        assertTrue(queue.isActive());
        assertEquals(2, QueueEventsTransfer.getInstance().getMaxThreadCount());
        MockClusterServiceImpl.getInstance().generateNodeLost(2, System.currentTimeMillis());
        MockClusterServiceImpl.getInstance().generateNodeLost(3, System.currentTimeMillis());
        Thread.currentThread().sleep(1000);

        Collection<Event> events = TestDataStore.getInstance().getEvents(1);
        for (int i = 0; i < 1000; i++) {
            assertTrue(exists(events, String.valueOf(i)));
        }
        assertEquals(events.size(), 1000);

        events = TestDataStore.getInstance().getEvents(2);
        assertEquals(events.size(), 500);
        events = TestDataStore.getInstance().getEvents(3);
        assertEquals(events.size(), 500);

        for (int i = 0; i < 1000; i++) {
            queue.getManager().getTransactionManager().begin();
            Collection<Event> take = queue.take(1);
            queue.getManager().getTransactionManager().commit();
        }

        Thread.sleep(1000);
        events = TestDataStore.getInstance().getEvents(1);
        for (int i = 0; i < 1000; i++) {
            assertTrue(exists(events, String.valueOf(1000 + i)));
        }
        assertEquals(events.size(), 1000);
        events = TestDataStore.getInstance().getEvents(2);
        assertEquals(events.size(), 0);

        events = TestDataStore.getInstance().getEvents(3);
        assertEquals(events.size(), 0);

        queue.getManager().getTransactionManager().begin();
        Collection<Event> take = queue.take(10000);
        queue.getManager().getTransactionManager().commit();

    }

    public void testNodeLost_CheckWaitingForTransfer() throws SinkException, SourceException, InterruptedException {
        ArrayList<EventWithKey> list1 = new ArrayList<EventWithKey>();
        ArrayList<EventWithKey> list2 = new ArrayList<EventWithKey>();
        ArrayList<EventWithKey> list3 = new ArrayList<EventWithKey>();
        for (int i = 0; i < 1000; i++) {
            list1.add(new EventWithKey(createEvent(String.valueOf(i))));
        }

        for (int i = 0; i < 500; i++) {
            list2.add(new EventWithKey(createEvent(String.valueOf(1000 + i))));
            list3.add(new EventWithKey(createEvent(String.valueOf(1500 + i))));
        }
        TestDataStore.getInstance().add(1, list1);
        TestDataStore.getInstance().add(2, list2);
        TestDataStore.getInstance().add(3, list3);
        QueueEventsTransfer.getInstance().setMaxThreadCount(2);
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 1000, queue.size());
        assertEquals("Wrong size", 1000, queue.getEstimatedCount());
        assertTrue(queue.isActive());
        assertEquals(2, QueueEventsTransfer.getInstance().getMaxThreadCount());
        MockClusterServiceImpl.getInstance().generateNodeLost(2, System.currentTimeMillis());
        MockClusterServiceImpl.getInstance().generateNodeLost(3, System.currentTimeMillis());
        Thread.currentThread().sleep(1000);

        Collection<Event> events = TestDataStore.getInstance().getEvents(1);
        for (int i = 0; i < 1000; i++) {
            assertTrue(exists(events, String.valueOf(i)));
        }
        assertEquals(events.size(), 1000);

        events = TestDataStore.getInstance().getEvents(2);
        assertEquals(events.size(), 500);
        events = TestDataStore.getInstance().getEvents(3);
        assertEquals(events.size(), 500);

        for (int i = 0; i < 1000; i++) {
            queue.getManager().getTransactionManager().begin();
            Collection<Event> take = queue.take(1);
            queue.getManager().getTransactionManager().commit();

            queue.getManager().getTransactionManager().begin();
            try {
                try {
                    queue.put(Arrays.asList(createEvent("Test_Event" + i)));
                    fail("Expected " + SinkException.Clogged.class);
                } catch (SinkException.Clogged e) {
                }
                queue.getManager().getTransactionManager().commit();
            } catch (Throwable e) {
                queue.getManager().getTransactionManager().rollback();
            }

            queue.getManager().getTransactionManager().begin();
            try {
                assertEquals("WrongSize", 1, queue.tryPut(Arrays.asList(createEvent("Test_try_Event" + i))).size());
                queue.getManager().getTransactionManager().commit();
            } catch (Throwable e) {
                queue.getManager().getTransactionManager().rollback();
            }
        }

        Thread.sleep(1000);
        events = TestDataStore.getInstance().getEvents(1);
        for (int i = 0; i < 1000; i++) {
            assertTrue(exists(events, String.valueOf(1000 + i)));
        }
        assertEquals(events.size(), 1000);
        events = TestDataStore.getInstance().getEvents(2);
        assertEquals(events.size(), 0);

        events = TestDataStore.getInstance().getEvents(3);
        assertEquals(events.size(), 0);

        queue.getManager().getTransactionManager().begin();
        Collection<Event> take = queue.take(10000);
        queue.getManager().getTransactionManager().commit();

        Thread.sleep(1000);
        queue.getManager().getTransactionManager().begin();
        queue.put(Arrays.asList(createEvent("Test_Event")));
        queue.getManager().getTransactionManager().commit();

        queue.getManager().getTransactionManager().begin();
        assertNull(queue.tryPut(Arrays.asList(createEvent("Test_try_Event"))));
        queue.getManager().getTransactionManager().commit();

    }

    public void testNodeLost_Disactivate_current() throws SinkException, SourceException, InterruptedException {
        ArrayList<EventWithKey> list1 = new ArrayList<EventWithKey>();
        ArrayList<EventWithKey> list2 = new ArrayList<EventWithKey>();
        ArrayList<EventWithKey> list3 = new ArrayList<EventWithKey>();
        for (int i = 0; i < 1000; i++) {
            list1.add(new EventWithKey(createEvent(String.valueOf(i))));
        }

        for (int i = 0; i < 500; i++) {
            list2.add(new EventWithKey(createEvent(String.valueOf(1000 + i))));
            list3.add(new EventWithKey(createEvent(String.valueOf(1500 + i))));
        }
        TestDataStore.getInstance().add(1, list1);
        TestDataStore.getInstance().add(2, list2);
        TestDataStore.getInstance().add(3, list3);
        QueueEventsTransfer.getInstance().setMaxThreadCount(2);
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 1000, queue.size());
        assertEquals("Wrong size", 1000, queue.getEstimatedCount());
        assertTrue(queue.isActive());
        assertEquals(2, QueueEventsTransfer.getInstance().getMaxThreadCount());
        MockClusterServiceImpl.getInstance().generateNodeLost(2, System.currentTimeMillis());
        MockClusterServiceImpl.getInstance().generateNodeLost(3, System.currentTimeMillis());
        Thread.currentThread().sleep(1000);

        Collection<Event> events = TestDataStore.getInstance().getEvents(1);
        for (int i = 0; i < 1000; i++) {
            assertTrue(exists(events, String.valueOf(i)));
        }
        assertEquals(events.size(), 1000);

        events = TestDataStore.getInstance().getEvents(2);
        assertEquals(events.size(), 500);
        events = TestDataStore.getInstance().getEvents(3);
        assertEquals(events.size(), 500);

        for (int i = 0; i < 500; i++) {
            queue.getManager().getTransactionManager().begin();
            Collection<Event> take = queue.take(1);
            queue.getManager().getTransactionManager().commit();
        }

        queue.getManager().getTransactionManager().begin();
        Collection<Event> take = queue.take(1);
        MockClusterServiceImpl.getInstance().generateCurrentNodeLost();
        queue.getManager().getTransactionManager().commit();

        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        MockClusterServiceImpl.getInstance().generateNodeLost(2, System.currentTimeMillis());
        MockClusterServiceImpl.getInstance().generateNodeLost(3, System.currentTimeMillis());
        Thread.currentThread().sleep(1000);
        for (int i = 0; i < 499; i++) {
            queue.getManager().getTransactionManager().begin();
            take = queue.take(1);
            queue.getManager().getTransactionManager().commit();
        }


        Thread.sleep(1000);
        events = TestDataStore.getInstance().getEvents(1);
        for (int i = 0; i < 1000; i++) {
            assertTrue(exists(events, String.valueOf(1000 + i)));
        }

        assertEquals(events.size(), 1000);

        events = TestDataStore.getInstance().getEvents(2);
        assertEquals(events.size(), 0);

        events = TestDataStore.getInstance().getEvents(3);
        assertEquals(events.size(), 0);

        queue.getManager().getTransactionManager().begin();
        take = queue.take(10000);
        queue.getManager().getTransactionManager().commit();

    }


    public void testNodeLost_ReactivateLost() throws SinkException, SourceException, InterruptedException {
        ArrayList<EventWithKey> list1 = new ArrayList<EventWithKey>();
        ArrayList<EventWithKey> list2 = new ArrayList<EventWithKey>();
        for (int i = 0; i < 1000; i++) {
            list1.add(new EventWithKey(createEvent(String.valueOf(i))));
            list2.add(new EventWithKey(createEvent(String.valueOf(1000 + i))));
        }
        TestDataStore.getInstance().add(1, list1);
        TestDataStore.getInstance().add(2, list2);

        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 1000, queue.size());
        assertEquals("Wrong size", 1000, queue.getEstimatedCount());
        assertTrue(queue.isActive());
        QueueEventsTransfer.getInstance().setMaxThreadCount(1);
        assertEquals(1, QueueEventsTransfer.getInstance().getMaxThreadCount());
        MockClusterServiceImpl.getInstance().generateNodeLost(2, System.currentTimeMillis());
        Thread.currentThread().sleep(1000);

        Collection<Event> events = TestDataStore.getInstance().getEvents(1);
        for (int i = 0; i < 1000; i++) {
            assertTrue(exists(events, String.valueOf(i)));
        }
        assertEquals(events.size(), 1000);

        events = TestDataStore.getInstance().getEvents(2);
        assertEquals(events.size(), 1000);

        for (int i = 0; i < 500; i++) {
            queue.getManager().getTransactionManager().begin();
            Collection<Event> take = queue.take(1);
            queue.getManager().getTransactionManager().commit();
            MockClusterServiceImpl.getInstance().setLastNodeActivity(2, System.currentTimeMillis());
        }

        Thread.sleep(1000);
        MockClusterServiceImpl.getInstance().generateNodeLost(2, System.currentTimeMillis());
        for (int i = 0; i < 500; i++) {
            queue.getManager().getTransactionManager().begin();
            Collection<Event> take = queue.take(1);
            queue.getManager().getTransactionManager().commit();
        }

        Thread.sleep(1000);
        events = TestDataStore.getInstance().getEvents(1);
        for (int i = 0; i < 1000; i++) {
            assertTrue(exists(events, String.valueOf(1000 + i)));
        }
        assertEquals(events.size(), 1000);
        events = TestDataStore.getInstance().getEvents(2);
        assertEquals(events.size(), 0);

        queue.getManager().getTransactionManager().begin();
        Collection<Event> take = queue.take(10000);
        queue.getManager().getTransactionManager().commit();

    }


    public void testNodeLost_Transfer_Null() throws SinkException, SourceException, InterruptedException {
        TestDataStore.getInstance().add(1, Arrays.asList(
                new EventWithKey(createEvent("1")),
                new EventWithKey(createEvent("2")),
                new EventWithKey(createEvent("3"))));
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 3, queue.size());
        assertEquals("Wrong size", 3, queue.getEstimatedCount());
        assertTrue(queue.isActive());

        controller.setReturnNullONTransfer(true);
        MockClusterServiceImpl.getInstance().generateNodeLost(2, System.currentTimeMillis());

        Thread.currentThread().sleep(1000);
        Collection<Event> events = TestDataStore.getInstance().getEvents(1);
        assertTrue(exists(events, "1"));
        assertTrue(exists(events, "2"));
        assertTrue(exists(events, "3"));
        assertEquals(events.size(), 3);

        events = TestDataStore.getInstance().getEvents(2);
        assertNull(events);

        queue.getManager().getTransactionManager().begin();
        Collection<Event> take = queue.take(1000);
        queue.getManager().getTransactionManager().commit();

        events = TestDataStore.getInstance().getEvents(1);
        assertTrue(!exists(events, "1"));
        assertTrue(!exists(events, "2"));
        assertTrue(!exists(events, "3"));
        assertEquals(events.size(), 0);
    }

    public void testNodeLost_Transfer_WithError() throws SinkException, SourceException, InterruptedException {
        TestDataStore.getInstance().add(1, Arrays.asList(
                new EventWithKey(createEvent("1")),
                new EventWithKey(createEvent("2")),
                new EventWithKey(createEvent("3"))));

        TestDataStore.getInstance().add(2, Arrays.asList(
                new EventWithKey(createEvent("4")),
                new EventWithKey(createEvent("5")),
                new EventWithKey(createEvent("6"))));
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 3, queue.size());
        assertEquals("Wrong size", 3, queue.getEstimatedCount());
        assertTrue(queue.isActive());

        controller.setErrorONTransfer(true);
        MockClusterServiceImpl.getInstance().generateNodeLost(2, System.currentTimeMillis());

        Thread.currentThread().sleep(1000);
        Collection<Event> events = TestDataStore.getInstance().getEvents(1);
        assertTrue(exists(events, "1"));
        assertTrue(exists(events, "2"));
        assertTrue(exists(events, "3"));
        assertEquals(events.size(), 3);

        controller.setErrorONTransfer(false);
        Thread.currentThread().sleep(1000);

        events = TestDataStore.getInstance().getEvents(1);
        assertTrue(exists(events, "1"));
        assertTrue(exists(events, "2"));
        assertTrue(exists(events, "3"));
        assertTrue(exists(events, "4"));
        assertTrue(exists(events, "5"));
        assertTrue(exists(events, "6"));
        assertEquals(events.size(), 6);

        events = TestDataStore.getInstance().getEvents(2);
        assertEquals(0, events.size());
    }

    public void testTransfer_interrupt() throws SinkException, SourceException, InterruptedException {
        ArrayList<EventWithKey> list1 = new ArrayList<EventWithKey>();
        ArrayList<EventWithKey> list2 = new ArrayList<EventWithKey>();
        for (int i = 0; i < 1000; i++) {
            list1.add(new EventWithKey(createEvent(String.valueOf(i))));
            list2.add(new EventWithKey(createEvent(String.valueOf(1000 + i))));
        }
        TestDataStore.getInstance().add(1, list1);
        TestDataStore.getInstance().add(2, list2);

        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 1000, queue.size());
        assertEquals("Wrong size", 1000, queue.getEstimatedCount());
        assertTrue(queue.isActive());
        QueueEventsTransfer.getInstance().setMaxThreadCount(1);
        assertEquals(1, QueueEventsTransfer.getInstance().getMaxThreadCount());
        MockClusterServiceImpl.getInstance().generateNodeLost(2, System.currentTimeMillis());
        Thread.currentThread().sleep(1000);

        Collection<Event> events = TestDataStore.getInstance().getEvents(1);
        for (int i = 0; i < 1000; i++) {
            assertTrue(exists(events, String.valueOf(i)));
        }
        assertEquals(events.size(), 1000);

        events = TestDataStore.getInstance().getEvents(2);
        assertEquals(events.size(), 1000);

        for (int i = 0; i < 500; i++) {
            queue.getManager().getTransactionManager().begin();
            Collection<Event> take = queue.take(1);
            queue.getManager().getTransactionManager().commit();
        }

        QueueEventsTransfer.getInstance().shutdown();

        Thread.sleep(1000);

        queue.getManager().getTransactionManager().begin();
        Collection<Event> take = queue.take(10000);
        queue.getManager().getTransactionManager().commit();

        QueueEventsTransfer.getInstance().start();
    }

    public void testTransfer_interrupt_1() throws SinkException, SourceException, InterruptedException {
        ArrayList<EventWithKey> list1 = new ArrayList<EventWithKey>();
        ArrayList<EventWithKey> list2 = new ArrayList<EventWithKey>();
        for (int i = 0; i < 1000; i++) {
            list1.add(new EventWithKey(createEvent(String.valueOf(i))));
            list2.add(new EventWithKey(createEvent(String.valueOf(1000 + i))));
        }
        TestDataStore.getInstance().add(1, list1);
        TestDataStore.getInstance().add(2, list2);

        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 1000, queue.size());
        assertEquals("Wrong size", 1000, queue.getEstimatedCount());
        assertTrue(queue.isActive());
        QueueEventsTransfer.getInstance().setMaxThreadCount(1);
        assertEquals(1, QueueEventsTransfer.getInstance().getMaxThreadCount());
        Thread.currentThread().sleep(1000);

        Collection<Event> events = TestDataStore.getInstance().getEvents(1);
        for (int i = 0; i < 1000; i++) {
            assertTrue(exists(events, String.valueOf(i)));
        }
        assertEquals(events.size(), 1000);

        events = TestDataStore.getInstance().getEvents(2);
        assertEquals(events.size(), 1000);

        for (int i = 0; i < 500; i++) {
            queue.getManager().getTransactionManager().begin();
            Collection<Event> take = queue.take(1);
            queue.getManager().getTransactionManager().commit();
        }

        Thread.currentThread().interrupt();

        try {
            queue.transfer(2);
            fail("Expected " + SinkException.Closed.class);
        } catch (SinkException.Closed e) {
        }

        queue.getManager().getTransactionManager().begin();
        Collection<Event> take = queue.take(10000);
        queue.getManager().getTransactionManager().commit();

    }

    public void testTransfer_interrupt_2() throws SinkException, SourceException, InterruptedException {
        ArrayList<EventWithKey> list1 = new ArrayList<EventWithKey>();
        ArrayList<EventWithKey> list2 = new ArrayList<EventWithKey>();
        for (int i = 0; i < 1000; i++) {
            list1.add(new EventWithKey(createEvent(String.valueOf(i))));
            list2.add(new EventWithKey(createEvent(String.valueOf(1000 + i))));
        }
        TestDataStore.getInstance().add(1, list1);
        TestDataStore.getInstance().add(2, list2);

        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 1000, queue.size());
        assertEquals("Wrong size", 1000, queue.getEstimatedCount());
        assertTrue(queue.isActive());
        QueueEventsTransfer.getInstance().setMaxThreadCount(1);
        assertEquals(1, QueueEventsTransfer.getInstance().getMaxThreadCount());
        Thread.currentThread().sleep(1000);

        Collection<Event> events = TestDataStore.getInstance().getEvents(1);
        for (int i = 0; i < 1000; i++) {
            assertTrue(exists(events, String.valueOf(i)));
        }
        assertEquals(events.size(), 1000);

        events = TestDataStore.getInstance().getEvents(2);
        assertEquals(events.size(), 1000);

        for (int i = 0; i < 500; i++) {
            queue.getManager().getTransactionManager().begin();
            Collection<Event> take = queue.take(1);
            queue.getManager().getTransactionManager().commit();
        }

        Thread.currentThread().interrupt();

        try {
            queue.waitForFreeSlots();
            fail("Expected " + SinkException.Closed.class);
        } catch (SinkException.Closed e) {
        }

        queue.getManager().getTransactionManager().begin();
        Collection<Event> take = queue.take(10000);
        queue.getManager().getTransactionManager().commit();

    }

    public void testTransfer_interrupt_3() throws SinkException, SourceException, InterruptedException {
        ArrayList<EventWithKey> list1 = new ArrayList<EventWithKey>();
        for (int i = 0; i < 1000; i++) {
            list1.add(new EventWithKey(createEvent(String.valueOf(i))));
        }
        TestDataStore.getInstance().add(1, list1);

        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        assertEquals("Wrong size", 1000, queue.size());
        assertEquals("Wrong size", 1000, queue.getEstimatedCount());
        assertTrue(queue.isActive());
        QueueEventsTransfer.getInstance().setMaxThreadCount(1);
        assertEquals(1, QueueEventsTransfer.getInstance().getMaxThreadCount());
        Thread.currentThread().sleep(1000);

        Collection<Event> events = TestDataStore.getInstance().getEvents(1);
        for (int i = 0; i < 1000; i++) {
            assertTrue(exists(events, String.valueOf(i)));
        }
        assertEquals(events.size(), 1000);

        MockClusterServiceImpl.getInstance().generateCurrentNodeLost();
        try {
            queue.transfer(2);
            fail("Expected " + SinkException.Closed.class);
        } catch (SinkException.Closed e) {
        }

        queue.getManager().getTransactionManager().begin();
        Collection<Event> take = queue.take(10000);
        queue.getManager().getTransactionManager().commit();

    }


    private boolean exists(Collection<Event> events, String contextId) {
        if (events == null) return false;
        for (Event e : events) {
            if (e.getContextId().equals(contextId)) {
                return true;
            }
        }

        return false;
    }


}
