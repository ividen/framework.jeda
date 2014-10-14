package ru.kwanza.jeda.core.queue;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.kwanza.jeda.api.*;
import ru.kwanza.jeda.api.internal.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Guzanov Alexander
 */
public class TestPriorityTransactionalMemoryQueueWithDSTrx extends TestCase {
    protected IJedaManager manager;
    private ClassPathXmlApplicationContext context;

    public static class StubJedaManager implements IJedaManager {
        private ITransactionManagerInternal tm;

        public StubJedaManager(ITransactionManagerInternal tm) {
            this.tm = tm;
        }

        public ITransactionManagerInternal getTransactionManager() {
            return tm;
        }

        public IStage getStage(String name) {
            return null;
        }

        public IStageInternal getStageInternal(String name) {
            return null;
        }

        public ITimer getTimer(String name) {
            return null;
        }

        public IPendingStore getPendingStore() {
            throw new UnsupportedOperationException("getPendingStore");
        }

        public IContextController getContextController(String name) {
            return null;
        }

        public IFlowBus getFlowBus(String name) {
            return null;
        }

        public IStageInternal getCurrentStage() {
            return null;
        }

        public void setCurrentStage(IStageInternal stage) {
        }

        public IStage registerStage(IStageInternal stage) {
            return null;
        }

        public IFlowBus registerFlowBus(String name, IFlowBus flowBus) {
            return null;
        }

        public IContextController registerContextController(String name, IContextController context) {
            return null;
        }

        public ITimer registerTimer(String name, ITimer timer) {
            return null;
        }

        public void registerObject(String name, Object object) {
        }

        public String resolveObjectName(Object object) {
            return null;
        }

        public Object resolveObject(String objectName) {
            return null;
        }
    }

    public static TestSuite suite() {
        return new TestSuite(TestPriorityTransactionalMemoryQueueWithDSTrx.class);
    }


    public void setUp() throws Exception {
        context = new ClassPathXmlApplicationContext(getContextPath(), TestPriorityTransactionalMemoryQueueWithDSTrx.class);
        manager = new StubJedaManager((ITransactionManagerInternal) context.getBean("transactionManager"));
    }

    public String getContextPath() {
        return "application-context-ds.xml";
    }

    public void tearDown() throws Exception {
        context.close();
    }


    public void testAllPriority() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("1", IPriorityEvent.Priority.LOW)}));
        manager.getTransactionManager().commit();

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("1", IPriorityEvent.Priority.NORMAL)}));
        manager.getTransactionManager().commit();

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("1", IPriorityEvent.Priority.HIGH)}));
        manager.getTransactionManager().commit();

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("1", IPriorityEvent.Priority.HIGHEST)}));
        manager.getTransactionManager().commit();

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("1", IPriorityEvent.Priority.CRITICAL)}));
        manager.getTransactionManager().commit();

        manager.getTransactionManager().begin();
        Collection<PriorityEvent> events = queue.take(10);
        assertEquals("WrongSize", 5, events.size());
        Iterator<PriorityEvent> iterator = events.iterator();
        assertEquals("Wrong Priority", IPriorityEvent.Priority.CRITICAL, iterator.next().getPriority());
        assertEquals("Wrong Priority", IPriorityEvent.Priority.HIGHEST, iterator.next().getPriority());
        assertEquals("Wrong Priority", IPriorityEvent.Priority.HIGH, iterator.next().getPriority());
        assertEquals("Wrong Priority", IPriorityEvent.Priority.NORMAL, iterator.next().getPriority());
        assertEquals("Wrong Priority", IPriorityEvent.Priority.LOW, iterator.next().getPriority());
        manager.getTransactionManager().commit();

        manager.getTransactionManager().begin();
        assertNull("Must be empty", queue.take(1));
        manager.getTransactionManager().commit();
    }

    public void testClogged_Begin_Begin_Commit_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();

        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9")})));


        manager.getTransactionManager().begin();

        assertEquals("Wrong clogged count!", 1, queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("11")})).size());

        assertEquals("Sink size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Sink size", 1, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Sink size", 10, queue.size());
    }

    public void testClogged_Begin_Begin_Rollback_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();

        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9")})));


        manager.getTransactionManager().begin();

        assertEquals("Wrong clogged count!", 1, queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("11")})).size());

        assertEquals("Sink size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Sink size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Sink size", 9, queue.size());
    }

    public void testClogged_Begin_Begin_Rollback_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();

        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9")})));


        manager.getTransactionManager().begin();

        assertEquals("Wrong clogged count!", 1, queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("11")})).size());

        assertEquals("Sink size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Sink size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Sink size", 0, queue.size());
    }

    public void testClogged_Begin_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();

        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9")})));


        assertEquals("Wrong clogged count!", 1, queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("11")})).size());

        assertEquals("Sink size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Sink size", 10, queue.size());
    }

    public void testClogged_Begin_Commit_Begin_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();

        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9")})));

        manager.getTransactionManager().commit();
        manager.getTransactionManager().begin();

        assertEquals("Wrong clogged count!", 1, queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("11")})).size());

        assertEquals("Sink size", 9, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Sink size", 10, queue.size());
    }

    public void testClogged_Begin_Rollback_Begin_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")})));


        manager.getTransactionManager().rollback();
        manager.getTransactionManager().begin();

        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("11")})));

        assertEquals("Sink size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Sink size", 2, queue.size());
    }

    public void testClogged_Begin_Rollback_Begin_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")})));


        manager.getTransactionManager().rollback();
        manager.getTransactionManager().begin();

        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("11")})));

        assertEquals("Sink size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Sink size", 0, queue.size());
    }

    public void testEmptyPutCommit() throws SinkException {
        IQueue queue = createQueue();
        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{}));
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 0, queue.size());
    }

    public void testEmptyPutRollback() throws SinkException {
        IQueue queue = createQueue();
        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{}));
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
    }

    public void testEmptyTryPutCommit() throws SinkException {
        IQueue queue = createQueue();
        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{})));
        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 0, queue.size());
    }

    public void testEmptyTryPutRollback() throws SinkException {
        IQueue queue = createQueue();
        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{})));
        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
    }

    public void testMaxSize() {
        PriorityTransactionalMemoryQueue memoryQueue = new PriorityTransactionalMemoryQueue(manager);
        assertEquals("MaxSize wrong", Long.MAX_VALUE, memoryQueue.getMaxSize());
    }

    public void testObserver() throws SinkException, SourceException {
        IQueue queue = createQueue();
        final ArrayList<Long> queueSize = new ArrayList<Long>();
        final ArrayList<Long> delta = new ArrayList<Long>();
        queue.setObserver(new IQueueObserver() {
            public void notifyChange(long s, long d) {
                queueSize.add(s);
                delta.add(d);
            }
        });

        assertNotNull("Must be NOT null", queue.getObserver());

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")}));
        manager.getTransactionManager().commit();

        manager.getTransactionManager().begin();
        try {
            queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("7"),
                    new PriorityEvent("7"),
                    new PriorityEvent("9"),
                    new PriorityEvent("10"),
                    new PriorityEvent("11")
            }));
        } catch (SinkException.Clogged e) {
        }
        manager.getTransactionManager().commit();


        manager.getTransactionManager().begin();
        assertEquals("Wrong decline size", 1, queue.tryPut(Arrays.asList(new IEvent[]{new PriorityEvent("7"),
                new PriorityEvent("7"),
                new PriorityEvent("9"),
                new PriorityEvent("10"),
                new PriorityEvent("11")
        })).size());
        manager.getTransactionManager().commit();

        manager.getTransactionManager().begin();
        assertEquals("Wrong count", 1, queue.take(1).size());
        manager.getTransactionManager().commit();

        manager.getTransactionManager().begin();
        assertEquals("Wrong count", 5, queue.take(5).size());
        manager.getTransactionManager().commit();

        manager.getTransactionManager().begin();
        assertEquals("Wrong count", 4, queue.take(10).size());
        manager.getTransactionManager().commit();


        assertEquals("Wrong notify size for 1 put", Long.valueOf(6L), queueSize.get(0));
        assertEquals("Wrong notify size for 2 put", Long.valueOf(10L), queueSize.get(1));
        assertEquals("Wrong notify size for 1 take", Long.valueOf(9l), queueSize.get(2));
        assertEquals("Wrong notify size for 2 take", Long.valueOf(4), queueSize.get(3));
        assertEquals("Wrong notify size for 3 take", Long.valueOf(0), queueSize.get(4));

        assertEquals("Wrong notify delta for 1 put", Long.valueOf(6L), delta.get(0));
        assertEquals("Wrong notify delta for 2 put", Long.valueOf(4L), delta.get(1));
        assertEquals("Wrong notify delta for 1 take", Long.valueOf(-1l), delta.get(2));
        assertEquals("Wrong notify delta for 2 take", Long.valueOf(-5), delta.get(3));
        assertEquals("Wrong notify delta for 3 take", Long.valueOf(-4), delta.get(4));

        queue.setObserver(null);
        queueSize.clear();
        delta.clear();


        assertNull("Must be null", queue.getObserver());

        manager.getTransactionManager().begin();
        try {
            queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("7"),
                    new PriorityEvent("7"),
                    new PriorityEvent("9"),
                    new PriorityEvent("10"),
                    new PriorityEvent("11")
            }));
        } catch (SinkException.Clogged e) {
        }
        manager.getTransactionManager().commit();

        manager.getTransactionManager().begin();
        assertEquals("Wrong count", 5, queue.take(10).size());
        manager.getTransactionManager().commit();

        assertEquals("Wrong count", 0, queueSize.size());
        assertEquals("Wrong count", 0, delta.size());
    }

    public void testPutCommit() throws SinkException, SourceException {
        PriorityTransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new PriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")}));

        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 6, queue.size());

        int i = 0;
        Collection<PriorityEvent> events = queue.take(1000);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("Wrong events count", 6, events.size());
        for (PriorityEvent e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }
    }

    public void testPutPutCommit() throws SinkException, SourceException {
        PriorityTransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")}));

        assertEquals("Wrong queue size", 0, queue.size());

        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")
        }));

        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 10, queue.size());
        int i = 0;
        Collection<PriorityEvent> events = queue.take(1000);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("Wrong events count", 10, events.size());
        for (PriorityEvent e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }
    }

    public void testPutPutRollback() throws SinkException, SourceException {
        PriorityTransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")}));

        assertEquals("Wrong queue size", 0, queue.size());

        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));

        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
    }

    public void testPutRollback() throws SinkException, SourceException {
        PriorityTransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")}));

        assertEquals("Wrong queue size", 0, queue.size());

        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));

        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
    }

    public void testPut_Begin_Begin_Commit_Commit() throws SinkException, SourceException {
        PriorityTransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")}));

        assertEquals("Wrong queue size", 0, queue.size());

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));

        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 4, queue.size());

        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 10, queue.size());
    }

    public void testPut_Begin_Begin_Rollback_Commit() throws SinkException, SourceException {
        PriorityTransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")}));

        assertEquals("Wrong queue size", 0, queue.size());

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));

        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());

        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 6, queue.size());
    }

    public void testPut_Begin_Begin_Rollback_Rollback() throws SinkException, SourceException {
        PriorityTransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")}));

        assertEquals("Wrong queue size", 0, queue.size());

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));

        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());

        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
    }

    public void testPut_Begin_Commit_Begin_Commit() throws SinkException, SourceException {
        PriorityTransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")}));

        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 6, queue.size());

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));

        assertEquals("Wrong queue size", 6, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 10, queue.size());
    }

    public void testPut_Begin_Rollback_Begin_Commit() throws SinkException, SourceException {
        PriorityTransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")}));

        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));

        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 4, queue.size());
    }

    public void testPut_Begin_Rollback_Begin_Rollback() throws SinkException, SourceException {
        PriorityTransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")}));

        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));

        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
    }

    public void testSinkExceptionClogged_Begin_Begin_Commit_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));
        manager.getTransactionManager().begin();
        try {
            queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                    new PriorityEvent("11")}));
            fail("Must be SinkException.Clogged");
        } catch (SinkException.Clogged e) {
        }
        assertEquals("Sink size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Sink size", 0, queue.size());

        manager.getTransactionManager().commit();
        assertEquals("Sink size", 10, queue.size());
    }

    public void testSinkExceptionClogged_Begin_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));

        try {
            queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                    new PriorityEvent("11")}));
            fail("Must be SinkException.Clogged");
        } catch (SinkException.Clogged e) {
        }

        assertEquals("Sink size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Sink size", 10, queue.size());
    }

    public void testSinkExceptionClogged_Begin_Commit_Begin_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));

        manager.getTransactionManager().commit();
        manager.getTransactionManager().begin();
        try {
            queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                    new PriorityEvent("11")}));
            fail("Must be SinkException.Clogged");
        } catch (SinkException.Clogged e) {
        }

        assertEquals("Sink size", 10, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Sink size", 10, queue.size());
    }

    public void testSinkExceptionClogged_Begin_Rollback_Begin_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));
        manager.getTransactionManager().rollback();
        manager.getTransactionManager().begin();
        try {
            queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                    new PriorityEvent("11")}));
        } catch (SinkException.Clogged e) {
            fail("Must NOT  be SinkException.Clogged");
        }
        assertEquals("Sink size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Sink size", 2, queue.size());
    }

    public void testSinkExceptionClogged_Begin_Rollback_Begin_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));
        manager.getTransactionManager().rollback();
        manager.getTransactionManager().begin();
        try {
            queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                    new PriorityEvent("11")}));
        } catch (SinkException.Clogged e) {
            fail("Must NOT  be SinkException.Clogged");
        }
        assertEquals("Sink size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Sink size", 0, queue.size());
    }

    public void testTakeBegin_Begin_Commit_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));
        manager.getTransactionManager().commit();

        manager.getTransactionManager().begin();
        Collection c = queue.take(5);
        assertEquals("Elemets size", 5, c.size());
        assertEquals("Queue size", 10, queue.size());


        manager.getTransactionManager().begin();
        c = queue.take(4);
        assertEquals("Elemets size", 4, c.size());
        assertEquals("Queue size", 10, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Qeuue size", 6, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Qeuue size", 1, queue.size());
    }

    public void testTakeBegin_Begin_Rollback_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));
        manager.getTransactionManager().commit();

        manager.getTransactionManager().begin();
        Collection c = queue.take(5);
        assertEquals("Elemets size", 5, c.size());
        assertEquals("Queue size", 10, queue.size());


        manager.getTransactionManager().begin();
        c = queue.take(4);
        assertEquals("Elemets size", 4, c.size());
        assertEquals("Queue size", 10, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Qeuue size", 10, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Qeuue size", 5, queue.size());
    }

    public void testTakeBegin_Begin_Rollback_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));
        manager.getTransactionManager().commit();

        manager.getTransactionManager().begin();
        Collection c = queue.take(5);
        assertEquals("Elemets size", 5, c.size());
        assertEquals("Queue size", 10, queue.size());


        manager.getTransactionManager().begin();
        c = queue.take(4);
        assertEquals("Elemets size", 4, c.size());
        assertEquals("Queue size", 10, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Qeuue size", 10, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Qeuue size", 10, queue.size());
    }

    public void testTakeBegin_Commit_Begin_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));
        manager.getTransactionManager().commit();

        manager.getTransactionManager().begin();
        Collection c = queue.take(5);
        assertEquals("Elemets size", 5, c.size());
        assertEquals("Queue size", 10, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 5, queue.size());

        manager.getTransactionManager().begin();
        c = queue.take(4);
        assertEquals("Elemets size", 4, c.size());
        assertEquals("Queue size", 5, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Qeuue size", 1, queue.size());
    }

    public void testTakeBegin_Rollback_Begin_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));
        manager.getTransactionManager().commit();

        manager.getTransactionManager().begin();
        Collection c = queue.take(5);
        assertEquals("Elemets size", 5, c.size());
        assertEquals("Queue size", 10, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Queue size", 10, queue.size());

        manager.getTransactionManager().begin();
        c = queue.take(4);
        assertEquals("Elemets size", 4, c.size());
        assertEquals("Queue size", 10, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Qeuue size", 6, queue.size());
    }

    public void testTakeBegin_Rollback_Begin_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));
        manager.getTransactionManager().commit();

        manager.getTransactionManager().begin();
        Collection c = queue.take(5);
        assertEquals("Elemets size", 5, c.size());
        assertEquals("Queue size", 10, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Queue size", 10, queue.size());

        manager.getTransactionManager().begin();
        c = queue.take(4);
        assertEquals("Elemets size", 4, c.size());
        assertEquals("Queue size", 10, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Qeuue size", 10, queue.size());
    }

    public void testTakeCommit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));
        manager.getTransactionManager().commit();

        manager.getTransactionManager().begin();
        Collection c = queue.take(100);
        assertEquals("Elemets size", 10, c.size());
        assertEquals("Queue size", 10, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Qeuue size", 0, queue.size());
    }

    public void testTakeRollback() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));
        manager.getTransactionManager().commit();

        manager.getTransactionManager().begin();
        Collection c = queue.take(100);
        assertEquals("Elemets size", 10, c.size());
        assertEquals("Queue size", 10, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Qeuue size", 10, queue.size());
    }

    public void testTakeTakeCommit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));
        manager.getTransactionManager().commit();

        manager.getTransactionManager().begin();
        Collection c = queue.take(5);
        assertEquals("Elemets size", 5, c.size());
        assertEquals("Queue size", 10, queue.size());
        c = queue.take(4);
        assertEquals("Elemets size", 4, c.size());
        assertEquals("Queue size", 10, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Qeuue size", 1, queue.size());
    }

    public void testTakeTakeRollback() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));
        manager.getTransactionManager().commit();

        manager.getTransactionManager().begin();
        Collection c = queue.take(5);
        assertEquals("Elemets size", 5, c.size());
        assertEquals("Queue size", 10, queue.size());
        c = queue.take(4);
        assertEquals("Elemets size", 4, c.size());
        assertEquals("Queue size", 10, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Qeuue size", 10, queue.size());
    }

    public void testTryPutCommit() throws SinkException, SourceException {
        PriorityTransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")})));

        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 6, queue.size());

        int i = 0;
        Collection<PriorityEvent> events = queue.take(1000);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("Wrong events count", 6, events.size());
        for (PriorityEvent e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }
    }

    public void testTryPutPutCommit() throws SinkException, SourceException {
        PriorityTransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")})));

        assertEquals("Wrong queue size", 0, queue.size());

        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")
        })));

        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 10, queue.size());
        int i = 0;
        Collection<PriorityEvent> events = queue.take(1000);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("Wrong events count", 10, events.size());
        for (PriorityEvent e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }
    }

    public void testTryPutPutRollback() throws SinkException, SourceException {
        PriorityTransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")})));

        assertEquals("Wrong queue size", 0, queue.size());

        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")})));

        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
    }

    public void testTryPutRollback() throws SinkException, SourceException {
        PriorityTransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")})));

        assertEquals("Wrong queue size", 0, queue.size());

        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")})));

        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
    }

    public void testTryPut_Begin_Begin_Commit_Commit() throws SinkException, SourceException {
        PriorityTransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")})));

        assertEquals("Wrong queue size", 0, queue.size());

        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")})));

        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 4, queue.size());

        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 10, queue.size());
    }

    public void testTryPut_Begin_Begin_Rollback_Commit() throws SinkException, SourceException {
        PriorityTransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")})));

        assertEquals("Wrong queue size", 0, queue.size());

        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")})));

        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());

        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 6, queue.size());
    }

    public void testTryPut_Begin_Begin_Rollback_Rollback() throws SinkException, SourceException {
        PriorityTransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")})));

        assertEquals("Wrong queue size", 0, queue.size());

        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")})));

        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());

        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
    }

    public void testTryPut_Begin_Commit_Begin_Commit() throws SinkException, SourceException {
        PriorityTransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")})));

        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 6, queue.size());

        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")})));

        assertEquals("Wrong queue size", 6, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 10, queue.size());
    }

    public void testTryPut_Begin_Rollback_Begin_Commit() throws SinkException, SourceException {
        PriorityTransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")})));

        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());

        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")})));

        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 4, queue.size());
    }

    public void testTryPut_Begin_Rollback_Begin_Rollback() throws SinkException, SourceException {
        PriorityTransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")})));

        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());

        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")})));

        assertEquals("Wrong queue size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
    }

    public void test_2_Queue_Put_Commit() throws SinkException, SourceException {
        IQueue queue1 = createQueue();
        IQueue queue2 = createQueue();
        manager.getTransactionManager().begin();
        queue1.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5")}));
        queue2.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("6"),
                new PriorityEvent("7")}));
        assertEquals("Queue size", 0, queue1.size());
        assertEquals("Queue size", 0, queue2.size());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 5, queue1.size());
        assertEquals("Queue size", 2, queue2.size());
    }

    public void test_2_Queue_Put_Rollback() throws SinkException, SourceException {
        IQueue queue1 = createQueue();
        IQueue queue2 = createQueue();
        manager.getTransactionManager().begin();
        queue1.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5")}));
        queue2.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("6"),
                new PriorityEvent("7")}));
        assertEquals("Queue size", 0, queue1.size());
        assertEquals("Queue size", 0, queue2.size());
        manager.getTransactionManager().rollback();
        assertEquals("Queue size", 0, queue1.size());
        assertEquals("Queue size", 0, queue2.size());
    }

    public void test_2_Queue_Take_Commit() throws SinkException, SourceException {
        IQueue queue1 = createQueue();
        IQueue queue2 = createQueue();
        manager.getTransactionManager().begin();
        queue1.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5")}));
        queue2.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("6"),
                new PriorityEvent("7")}));
        assertEquals("Queue size", 0, queue1.size());
        assertEquals("Queue size", 0, queue2.size());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 5, queue1.size());
        assertEquals("Queue size", 2, queue2.size());

        manager.getTransactionManager().begin();

        assertEquals("Evnt count", 3, queue1.take(3).size());
        assertEquals("Evnt count", 1, queue2.take(1).size());

        assertEquals("Queue size", 5, queue1.size());
        assertEquals("Queue size", 2, queue2.size());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 2, queue1.size());
        assertEquals("Queue size", 1, queue2.size());
    }

    public void test_2_Queue_Take_Rollback() throws SinkException, SourceException {
        IQueue queue1 = createQueue();
        IQueue queue2 = createQueue();
        manager.getTransactionManager().begin();
        queue1.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5")}));
        queue2.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("6"),
                new PriorityEvent("7")}));
        assertEquals("Queue size", 0, queue1.size());
        assertEquals("Queue size", 0, queue2.size());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 5, queue1.size());
        assertEquals("Queue size", 2, queue2.size());

        manager.getTransactionManager().begin();

        assertEquals("Evnt count", 3, queue1.take(3).size());
        assertEquals("Evnt count", 1, queue2.take(1).size());

        assertEquals("Queue size", 5, queue1.size());
        assertEquals("Queue size", 2, queue2.size());
        manager.getTransactionManager().rollback();
        assertEquals("Queue size", 5, queue1.size());
        assertEquals("Queue size", 2, queue2.size());
    }

    public void test_Begin_Put_Begin_Take_Commit_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));

        manager.getTransactionManager().begin();
        Collection c = queue.take(100);
        assertNull("Must be empty", c);
        assertEquals("Queue size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Qeuue size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Qeuue size", 10, queue.size());
    }

    public void test_Begin_Put_Begin_Take_Rollback_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));

        manager.getTransactionManager().begin();
        Collection c = queue.take(100);
        assertNull("Must be empty", c);
        assertEquals("Queue size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Qeuue size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Qeuue size", 10, queue.size());
    }

    public void test_Begin_Put_Begin_Take_Rollback_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));

        manager.getTransactionManager().begin();
        Collection c = queue.take(100);
        assertNull("Must be empty", c);
        assertEquals("Queue size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Qeuue size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Qeuue size", 0, queue.size());
    }

    public void test_Begin_Put_Commit_Begin_Take_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));

        assertEquals("Queue size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 10, queue.size());

        manager.getTransactionManager().begin();
        Collection c = queue.take(100);
        assertEquals("Must be empty", 10, c.size());
        assertEquals("Queue size", 10, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Qeuue size", 0, queue.size());
    }

    public void test_Begin_Put_Commit_Begin_Take_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));

        assertEquals("Queue size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 10, queue.size());

        manager.getTransactionManager().begin();
        Collection c = queue.take(100);
        assertEquals("Must Not be empty", 10, c.size());
        assertEquals("Queue size", 10, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Qeuue size", 10, queue.size());
    }

    public void test_Begin_Put_Rollback_Begin_Take_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));

        assertEquals("Queue size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Queue size", 0, queue.size());

        manager.getTransactionManager().begin();
        Collection c = queue.take(100);
        assertNull("Must be empty", c);
        assertEquals("Queue size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Qeuue size", 0, queue.size());
    }

    public void test_Begin_Put_Rollback_Begin_Take_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));

        assertEquals("Queue size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Queue size", 0, queue.size());

        manager.getTransactionManager().begin();
        Collection c = queue.take(100);
        assertNull("Must be empty", c);
        assertEquals("Queue size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Qeuue size", 0, queue.size());
    }

    public void test_Begin_Take_Begin_Put_Commit_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5")}));
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 5, queue.size());

        manager.getTransactionManager().begin();
        Collection c = queue.take(2);
        assertEquals("Must be empty", 2, c.size());
        assertEquals("Queue size", 5, queue.size());

        manager.getTransactionManager().begin();
        assertEquals("Clogged Size : ", 1, queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10"), new PriorityEvent("11")})).size());

        assertEquals("Queue size", 5, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 10, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 8, queue.size());
    }

    public void test_Begin_Take_Begin_Put_Commit_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5")}));
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 5, queue.size());

        manager.getTransactionManager().begin();
        Collection c = queue.take(2);
        assertEquals("Must be empty", 2, c.size());
        assertEquals("Queue size", 5, queue.size());

        manager.getTransactionManager().begin();
        assertEquals("Clogged Size : ", 1, queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10"), new PriorityEvent("11")})).size());

        assertEquals("Queue size", 5, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 10, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Queue size", 10, queue.size());
    }

    public void test_Begin_Take_Begin_Put_Rollback_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5")}));
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 5, queue.size());

        manager.getTransactionManager().begin();
        Collection c = queue.take(2);
        assertEquals("Must be empty", 2, c.size());
        assertEquals("Queue size", 5, queue.size());

        manager.getTransactionManager().begin();
        assertEquals("Clogged Size : ", 1, queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10"), new PriorityEvent("11")})).size());

        assertEquals("Queue size", 5, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Queue size", 5, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 3, queue.size());
    }

    public void test_Begin_Take_Begin_Put_Rollback_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5")}));
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 5, queue.size());

        manager.getTransactionManager().begin();
        Collection c = queue.take(2);
        assertEquals("Must be empty", 2, c.size());
        assertEquals("Queue size", 5, queue.size());

        manager.getTransactionManager().begin();
        assertEquals("Clogged Size : ", 1, queue.tryPut(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10"), new PriorityEvent("11")})).size());

        assertEquals("Queue size", 5, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Queue size", 5, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Queue size", 5, queue.size());
    }

    public void test_Begin_Take_Commit_Begin_Put_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5")}));
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 5, queue.size());

        manager.getTransactionManager().begin();
        Collection c = queue.take(2);
        assertEquals("Must be empty", 2, c.size());
        assertEquals("Queue size", 5, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Qeuue size", 3, queue.size());

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));
        assertEquals("Queue size", 3, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 8, queue.size());
    }

    public void test_Begin_Take_Rollback_Begin_Put_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5")}));
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 5, queue.size());

        manager.getTransactionManager().begin();
        Collection c = queue.take(2);
        assertEquals("Must be empty", 2, c.size());
        assertEquals("Queue size", 5, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Qeuue size", 5, queue.size());

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IPriorityEvent[]{new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")}));
        assertEquals("Queue size", 5, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 10, queue.size());
    }

    public void test_Clone_Copy() throws SinkException, SourceException {
        IQueue queue1 = new PriorityTransactionalMemoryQueue(manager, ObjectCloneType.CLONE, 10l);

        manager.getTransactionManager().begin();
        PriorityEvent event = new PriorityEvent("1");
        queue1.put(Arrays.asList(new IPriorityEvent[]{event}));
        assertEquals("Queue size", 0, queue1.size());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 1, queue1.size());

        manager.getTransactionManager().begin();

        Collection take = queue1.take(1);
        assertEquals("Evnt count", 1, take.size());

        PriorityEvent event_copy = (PriorityEvent) take.iterator().next();

        assertEquals("Reference equal", event, event_copy);
        manager.getTransactionManager().commit();
    }


    public void test_None_Copy() throws SinkException, SourceException {
        IQueue queue1 = new PriorityTransactionalMemoryQueue(manager, ObjectCloneType.NONE, 10l);

        manager.getTransactionManager().begin();
        PriorityEvent event = new PriorityEvent("1");
        queue1.put(Arrays.asList(new IPriorityEvent[]{event}));
        assertEquals("Queue size", 0, queue1.size());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 1, queue1.size());

        manager.getTransactionManager().begin();

        Collection take = queue1.take(1);
        assertEquals("Evnt count", 1, take.size());

        PriorityEvent event_copy = (PriorityEvent) take.iterator().next();

        assertTrue("Reference equal", event == event_copy);
        manager.getTransactionManager().commit();
    }

    public void test_Non_Clone_Copy() throws SinkException, SourceException {
        IQueue queue1 = new PriorityTransactionalMemoryQueue(manager, ObjectCloneType.CLONE, 10l);

        manager.getTransactionManager().begin();
        NonSerializablePriorityEvent event = new NonSerializablePriorityEvent("1");
        queue1.put(Arrays.asList(new IPriorityEvent[]{event}));
        assertEquals("Queue size", 0, queue1.size());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 1, queue1.size());

        manager.getTransactionManager().begin();

        Collection take = queue1.take(1);
        assertEquals("Evnt count", 1, take.size());

        NonSerializablePriorityEvent event_copy = (NonSerializablePriorityEvent) take.iterator().next();

        assertTrue("Reference equal", (event == event_copy));
        manager.getTransactionManager().commit();
    }

    public void test_Non_Serialization_Copy() throws SinkException, SourceException {
        IQueue queue1 = createQueue();

        manager.getTransactionManager().begin();
        NonSerializablePriorityEvent event = new NonSerializablePriorityEvent("1");
        queue1.put(Arrays.asList(new IPriorityEvent[]{event}));
        assertEquals("Queue size", 0, queue1.size());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 1, queue1.size());

        manager.getTransactionManager().begin();

        Collection take = queue1.take(1);
        assertEquals("Evnt count", 1, take.size());

        manager.getTransactionManager().rollback();

        manager.getTransactionManager().begin();

        take = queue1.take(1);
        assertEquals("Event count", 1, take.size());

        manager.getTransactionManager().commit();

        NonSerializablePriorityEvent event_copy = (NonSerializablePriorityEvent) take.iterator().next();

        assertTrue("Reference equal", (event == event_copy));
    }

    public void test_Serialization_Copy() throws SinkException, SourceException {
        IQueue queue1 = createQueue();

        manager.getTransactionManager().begin();
        PriorityEvent event = new PriorityEvent("1");
        queue1.put(Arrays.asList(new IPriorityEvent[]{event}));
        assertEquals("Queue size", 0, queue1.size());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 1, queue1.size());

        manager.getTransactionManager().begin();
        Collection take = queue1.take(1);
        assertEquals("Evnt count", 1, take.size());
        manager.getTransactionManager().rollback();

        manager.getTransactionManager().begin();
        take = queue1.take(1);
        assertEquals("Evnt count", 1, take.size());
        PriorityEvent event_copy = (PriorityEvent) take.iterator().next();
        manager.getTransactionManager().commit();

        assertNotSame("Reference must not be equal", event, event_copy);
        assertEquals("Objects equals", event, event_copy);
    }

    protected PriorityTransactionalMemoryQueue createQueue() {
        return new PriorityTransactionalMemoryQueue(manager, ObjectCloneType.SERIALIZE, 10l);
    }
}
