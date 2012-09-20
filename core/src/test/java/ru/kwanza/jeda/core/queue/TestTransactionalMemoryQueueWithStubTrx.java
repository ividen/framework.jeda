package ru.kwanza.jeda.core.queue;

import ru.kwanza.jeda.api.*;
import ru.kwanza.jeda.api.internal.*;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.transaction.xa.XAResource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Guzanov Alexander
 */
public class TestTransactionalMemoryQueueWithStubTrx extends TestCase {
    protected ISystemManager manager;

    public static class StubTransaction implements Transaction {
        private ArrayList<Synchronization> sync = new ArrayList<Synchronization>();


        public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
            for (Synchronization s : sync) {
                s.beforeCompletion();
                s.afterCompletion(Status.STATUS_COMMITTED);
            }
        }

        public boolean delistResource(XAResource xaResource, int i) throws IllegalStateException, SystemException {
            return false;
        }

        public boolean enlistResource(XAResource xaResource) throws RollbackException, IllegalStateException, SystemException {
            return false;
        }

        public int getStatus() throws SystemException {
            return 0;
        }

        public void registerSynchronization(Synchronization synchronization) throws RollbackException, IllegalStateException, SystemException {
            sync.add(synchronization);
        }

        public void rollback() throws IllegalStateException, SystemException {
            for (Synchronization s : sync) {
                s.beforeCompletion();
                s.afterCompletion(Status.STATUS_ROLLEDBACK);
            }
        }

        public void setRollbackOnly() throws IllegalStateException, SystemException {
        }
    }

    public static class StubTM implements ITransactionManagerInternal {
        StubTransaction currentTx;
        LinkedList<StubTransaction> txs = new LinkedList<StubTransaction>();


        public void begin() {
            synchronized (this) {
                currentTx = new StubTransaction();
                txs.add(currentTx);
            }
        }

        public void commit() {
            synchronized (this) {
                try {
                    currentTx.commit();
                    iterateTx();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void rollback() {
            synchronized (this) {
                try {
                    currentTx.rollback();
                    iterateTx();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public boolean hasTransaction() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Transaction getTransaction() {
            return currentTx;
        }

        public void suspend() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void resume() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void rollbackAllActive() {

        }

        private void iterateTx() {
            txs.removeLast();
            if (!txs.isEmpty()) {
                currentTx = txs.getLast();
            } else {
                currentTx = null;
            }
        }
    }

    public static class StubSystemManager implements ISystemManager {
        StubTM tm = new StubTM();


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
        return new TestSuite(TestTransactionalMemoryQueueWithStubTrx.class);
    }

    public void setUp() throws Exception {
        manager = new StubSystemManager();
    }

    public void tearDown() throws Exception {
    }

    public void testClogged_Begin_Begin_Commit_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();

        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9")})));


        manager.getTransactionManager().begin();

        assertEquals("Wrong clogged count!", 1, queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("11")})).size());

        assertEquals("Sink size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Sink size", 1, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Sink size", 10, queue.size());
    }

    public void testClogged_Begin_Begin_Rollback_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();

        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9")})));


        manager.getTransactionManager().begin();

        assertEquals("Wrong clogged count!", 1, queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("11")})).size());

        assertEquals("Sink size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Sink size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Sink size", 9, queue.size());
    }

    public void testClogged_Begin_Begin_Rollback_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();

        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9")})));


        manager.getTransactionManager().begin();

        assertEquals("Wrong clogged count!", 1, queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("11")})).size());

        assertEquals("Sink size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Sink size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Sink size", 0, queue.size());
    }

    public void testClogged_Begin_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();

        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9")})));


        assertEquals("Wrong clogged count!", 1, queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("11")})).size());

        assertEquals("Sink size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Sink size", 10, queue.size());
    }

    public void testClogged_Begin_Commit_Begin_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();

        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9")})));

        manager.getTransactionManager().commit();
        manager.getTransactionManager().begin();

        assertEquals("Wrong clogged count!", 1, queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("11")})).size());

        assertEquals("Sink size", 9, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Sink size", 10, queue.size());
    }

    public void testClogged_Begin_Rollback_Begin_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")})));


        manager.getTransactionManager().rollback();
        manager.getTransactionManager().begin();

        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("11")})));

        assertEquals("Sink size", 0, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Sink size", 2, queue.size());
    }

    public void testClogged_Begin_Rollback_Begin_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")})));


        manager.getTransactionManager().rollback();
        manager.getTransactionManager().begin();

        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("11")})));

        assertEquals("Sink size", 0, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Sink size", 0, queue.size());
    }

    public void testEmptyPutCommit() throws SinkException {
        IQueue queue = createQueue();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{}));
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
    }

    public void testEmptyPutRollback() throws SinkException {
        IQueue queue = createQueue();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{}));
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
    }

    public void testEmptyTryPutCommit() throws SinkException {
        IQueue queue = createQueue();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{})));
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
    }

    public void testEmptyTryPutRollback() throws SinkException {
        IQueue queue = createQueue();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{})));
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
    }

    public void testMaxSize() {
        TransactionalMemoryQueue memoryQueue = new TransactionalMemoryQueue(manager);
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
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));
        manager.getTransactionManager().commit();

        manager.getTransactionManager().begin();
        try {
            queue.put(Arrays.asList(new IEvent[]{new Event("7"),
                    new Event("7"),
                    new Event("9"),
                    new Event("10"),
                    new Event("11")
            }));
        } catch (SinkException.Clogged e) {
        }
        manager.getTransactionManager().commit();


        manager.getTransactionManager().begin();
        assertEquals("Wrong decline size", 1, queue.tryPut(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("7"),
                new Event("9"),
                new Event("10"),
                new Event("11")
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
            queue.put(Arrays.asList(new IEvent[]{new Event("7"),
                    new Event("7"),
                    new Event("9"),
                    new Event("10"),
                    new Event("11")
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
        IQueue<Event> queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new Event[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 6, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        int i = 0;
        Collection<Event> events = queue.take(1000);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
        assertEquals("Wrong events count", 6, events.size());
        for (Event e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }
    }

    public void testPutPutCommit() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        queue.put(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")
        }));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 10, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        int i = 0;
        Collection<Event> events = queue.take(1000);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
        assertEquals("Wrong events count", 10, events.size());
        for (Event e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }
    }

    public void testPutPutRollback() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        queue.put(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
    }

    public void testPutRollback() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        queue.put(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
    }

    public void testPut_Begin_Begin_Commit_Commit() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 4, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());

        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 10, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
    }

    public void testPut_Begin_Begin_Rollback_Commit() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 6, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());
    }

    public void testPut_Begin_Begin_Rollback_Rollback() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
    }

    public void testPut_Begin_Commit_Begin_Commit() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 6, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));

        assertEquals("Wrong queue size", 6, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 10, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
    }

    public void testPut_Begin_Rollback_Begin_Commit() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 4, queue.getEstimatedCount());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 4, queue.size());
        assertEquals("WrongEstimateQueueSize", 4, queue.getEstimatedCount());
    }

    public void testPut_Begin_Rollback_Begin_Rollback() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 4, queue.getEstimatedCount());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
    }

    public void testSinkExceptionClogged_Begin_Begin_Commit_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));
        manager.getTransactionManager().begin();
        try {
            queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                    new Event("11")}));
            fail("Must be SinkException.Clogged");
        } catch (SinkException.Clogged e) {
        }
        assertEquals("Sink size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        manager.getTransactionManager().commit();
        assertEquals("Sink size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());

        manager.getTransactionManager().commit();
        assertEquals("Sink size", 10, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
    }

    public void testSinkExceptionClogged_Begin_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));

        try {
            queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                    new Event("11")}));
            fail("Must be SinkException.Clogged");
        } catch (SinkException.Clogged e) {
        }

        assertEquals("Sink size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        manager.getTransactionManager().commit();
        assertEquals("Sink size", 10, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
    }

    public void testSinkExceptionClogged_Begin_Commit_Begin_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));

        manager.getTransactionManager().commit();
        manager.getTransactionManager().begin();
        try {
            queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                    new Event("11")}));
            fail("Must be SinkException.Clogged");
        } catch (SinkException.Clogged e) {
        }

        assertEquals("Sink size", 10, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        manager.getTransactionManager().commit();
        assertEquals("Sink size", 10, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
    }

    public void testSinkExceptionClogged_Begin_Rollback_Begin_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));
        manager.getTransactionManager().rollback();
        manager.getTransactionManager().begin();
        try {
            queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                    new Event("11")}));
        } catch (SinkException.Clogged e) {
            fail("Must NOT  be SinkException.Clogged");
        }
        assertEquals("Sink size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 2, queue.getEstimatedCount());
        manager.getTransactionManager().commit();
        assertEquals("Sink size", 2, queue.size());
        assertEquals("WrongEstimateQueueSize", 2, queue.getEstimatedCount());
    }

    public void testSinkExceptionClogged_Begin_Rollback_Begin_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));
        manager.getTransactionManager().rollback();
        manager.getTransactionManager().begin();
        try {
            queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                    new Event("11")}));
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
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));
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
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));
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
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));
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
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));
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
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));
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
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));
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
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));
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
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));
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
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));
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
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));
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

    public void testTakeZero() throws SourceException, SinkException {
        IQueue queue = createQueue();

        assertEquals("WrongQueueSize", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));
        assertEquals("WrongQueueSize", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());
        manager.getTransactionManager().commit();

        manager.getTransactionManager().begin();
        assertNull("Zero take ", queue.take(0));
        assertEquals("WrongQueueSize", 6, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());
        manager.getTransactionManager().commit();

        assertEquals("WrongQueueSize", 6, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());
    }

    public void testTryPutCommit() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 6, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        int i = 0;
        Collection<Event> events = queue.take(1000);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
        assertEquals("Wrong events count", 6, events.size());
        for (Event e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }
    }

    public void testTryPutInterrupt() throws SourceException, SinkException {
        IQueue queue = createQueue();

        assertEquals("WrongQueueSize", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
        manager.getTransactionManager().begin();
        Thread.currentThread().interrupt();
        try {
            try {
                queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                        new Event("2"),
                        new Event("3"),
                        new Event("4"),
                        new Event("5"),
                        new Event("6")}));
                fail("MUstbe SinkClosed");
                manager.getTransactionManager().commit();
            } catch (SinkException.Closed e) {
                assertEquals("WrongQueueSize", 0, queue.size());
                assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
                manager.getTransactionManager().rollback();
            }
        } catch (Throwable e) {
            assertEquals("WrongQueueSize", 0, queue.size());
            assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
            manager.getTransactionManager().rollback();
        }

        assertEquals("WrongQueueSize", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
    }

    public void testTryPutPutCommit() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")
        })));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 10, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        int i = 0;
        Collection<Event> events = queue.take(1000);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
        assertEquals("Wrong events count", 10, events.size());
        for (Event e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }
    }

    public void testTryPutPutRollback() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
    }

    public void testTryPutRollback() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
    }

    public void testTryPut_Begin_Begin_Commit_Commit() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 4, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());

        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 10, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
    }

    public void testTryPut_Begin_Begin_Rollback_Commit() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 6, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());
    }

    public void testTryPut_Begin_Begin_Rollback_Rollback() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
    }

    public void testTryPut_Begin_Commit_Begin_Commit() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 6, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")})));

        assertEquals("Wrong queue size", 6, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 10, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
    }

    public void testTryPut_Begin_Rollback_Begin_Commit() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());

        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 4, queue.getEstimatedCount());
        manager.getTransactionManager().commit();
        assertEquals("Wrong queue size", 4, queue.size());
        assertEquals("WrongEstimateQueueSize", 4, queue.getEstimatedCount());
    }

    public void testTryPut_Begin_Rollback_Begin_Rollback() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());

        manager.getTransactionManager().begin();
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 4, queue.getEstimatedCount());
        manager.getTransactionManager().rollback();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
    }

    public void test_2_Queue_Put_Commit() throws SinkException, SourceException {
        IQueue queue1 = createQueue();
        IQueue queue2 = createQueue();
        manager.getTransactionManager().begin();
        queue1.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5")}));
        queue2.put(Arrays.asList(new IEvent[]{new Event("6"),
                new Event("7")}));
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
        queue1.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5")}));
        queue2.put(Arrays.asList(new IEvent[]{new Event("6"),
                new Event("7")}));
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
        queue1.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5")}));
        queue2.put(Arrays.asList(new IEvent[]{new Event("6"),
                new Event("7")}));
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
        queue1.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5")}));
        queue2.put(Arrays.asList(new IEvent[]{new Event("6"),
                new Event("7")}));
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
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));

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
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));

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
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));

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
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));

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
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));

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
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));

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
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));

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
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5")}));
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 5, queue.size());

        manager.getTransactionManager().begin();
        Collection c = queue.take(2);
        assertEquals("Must be empty", 2, c.size());
        assertEquals("Queue size", 5, queue.size());

        manager.getTransactionManager().begin();
        assertEquals("Clogged Size : ", 1, queue.tryPut(Arrays.asList(new IEvent[]{new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10"), new Event("11")})).size());

        assertEquals("Queue size", 5, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 10, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 8, queue.size());
    }

    public void test_Begin_Take_Begin_Put_Commit_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5")}));
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 5, queue.size());

        manager.getTransactionManager().begin();
        Collection c = queue.take(2);
        assertEquals("Must be empty", 2, c.size());
        assertEquals("Queue size", 5, queue.size());

        manager.getTransactionManager().begin();
        assertEquals("Clogged Size : ", 1, queue.tryPut(Arrays.asList(new IEvent[]{new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10"), new Event("11")})).size());

        assertEquals("Queue size", 5, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 10, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Queue size", 10, queue.size());
    }

    public void test_Begin_Take_Begin_Put_Rollback_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5")}));
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 5, queue.size());

        manager.getTransactionManager().begin();
        Collection c = queue.take(2);
        assertEquals("Must be empty", 2, c.size());
        assertEquals("Queue size", 5, queue.size());

        manager.getTransactionManager().begin();
        assertEquals("Clogged Size : ", 1, queue.tryPut(Arrays.asList(new IEvent[]{new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10"), new Event("11")})).size());

        assertEquals("Queue size", 5, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Queue size", 5, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 3, queue.size());
    }

    public void test_Begin_Take_Begin_Put_Rollback_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5")}));
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 5, queue.size());

        manager.getTransactionManager().begin();
        Collection c = queue.take(2);
        assertEquals("Must be empty", 2, c.size());
        assertEquals("Queue size", 5, queue.size());

        manager.getTransactionManager().begin();
        assertEquals("Clogged Size : ", 1, queue.tryPut(Arrays.asList(new IEvent[]{new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10"), new Event("11")})).size());

        assertEquals("Queue size", 5, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Queue size", 5, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Queue size", 5, queue.size());
    }

    public void test_Begin_Take_Commit_Begin_Put_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5")}));
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 5, queue.size());

        manager.getTransactionManager().begin();
        Collection c = queue.take(2);
        assertEquals("Must be empty", 2, c.size());
        assertEquals("Queue size", 5, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Qeuue size", 3, queue.size());

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));
        assertEquals("Queue size", 3, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 8, queue.size());
    }

    public void test_Begin_Take_Rollback_Begin_Put_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5")}));
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 5, queue.size());

        manager.getTransactionManager().begin();
        Collection c = queue.take(2);
        assertEquals("Must be empty", 2, c.size());
        assertEquals("Queue size", 5, queue.size());
        manager.getTransactionManager().rollback();
        assertEquals("Qeuue size", 5, queue.size());

        manager.getTransactionManager().begin();
        queue.put(Arrays.asList(new IEvent[]{new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));
        assertEquals("Queue size", 5, queue.size());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 10, queue.size());
    }

    public void test_Clone_Copy() throws SinkException, SourceException {
        IQueue queue1 = new TransactionalMemoryQueue(manager, ObjectCloneType.CLONE, 10l);

        manager.getTransactionManager().begin();
        Event event = new Event("1");
        queue1.put(Arrays.asList(new IEvent[]{event}));
        assertEquals("Queue size", 0, queue1.size());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 1, queue1.size());

        manager.getTransactionManager().begin();

        Collection take = queue1.take(1);
        assertEquals("Evnt count", 1, take.size());

        Event event_copy = (Event) take.iterator().next();

        assertEquals("Reference equal", event, event_copy);
        manager.getTransactionManager().commit();
    }

    public void test_Non_Clone_Copy() throws SinkException, SourceException {
        IQueue queue1 = new TransactionalMemoryQueue(manager, ObjectCloneType.CLONE, 10l);

        manager.getTransactionManager().begin();
        NonSerializableEvent event = new NonSerializableEvent("1");
        queue1.put(Arrays.asList(new IEvent[]{event}));
        assertEquals("Queue size", 0, queue1.size());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 1, queue1.size());

        manager.getTransactionManager().begin();

        Collection take = queue1.take(1);
        assertEquals("Evnt count", 1, take.size());

        NonSerializableEvent event_copy = (NonSerializableEvent) take.iterator().next();

        assertTrue("Reference equal", (event == event_copy));
        manager.getTransactionManager().commit();
    }

    public void test_Non_Serialization_Copy() throws SinkException, SourceException {
        IQueue queue1 = createQueue();

        manager.getTransactionManager().begin();
        NonSerializableEvent event = new NonSerializableEvent("1");
        queue1.put(Arrays.asList(new IEvent[]{event}));
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

        manager.getTransactionManager().commit();

        NonSerializableEvent event_copy = (NonSerializableEvent) take.iterator().next();

        assertTrue("Reference equal", (event == event_copy));
    }

    public void test_Put_interrupt_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();

        manager.getTransactionManager().begin();
        Event event = new Event("1");
        queue.put(Arrays.asList(new IEvent[]{event}));
        assertEquals("Queue size", 0, queue.size());
        assertEquals("Queue size", 1, queue.getEstimatedCount());
        Thread.currentThread().interrupt();
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 0, queue.size());
        assertEquals("Queue size", 1, queue.getEstimatedCount());
    }

    public void test_Serialization_Copy() throws SinkException, SourceException {
        IQueue queue1 = createQueue();

        manager.getTransactionManager().begin();
        Event event = new Event("1");
        queue1.put(Arrays.asList(new IEvent[]{event}));
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
        Event event_copy = (Event) take.iterator().next();
        manager.getTransactionManager().commit();

        assertNotSame("Reference must not be equal", event, event_copy);
        assertEquals("Objects equals", event, event_copy);
    }

    public void test_TakeCLONE_Rollback() throws SinkException, SourceException {
        IQueue queue1 = new TransactionalMemoryQueue(manager, ObjectCloneType.CLONE, 10l);

        manager.getTransactionManager().begin();
        Event event = new Event("1");
        queue1.put(Arrays.asList(new IEvent[]{event}));
        assertEquals("Queue size", 0, queue1.size());
        assertEquals("Queue size", 1, queue1.getEstimatedCount());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 1, queue1.size());
        assertEquals("Queue size", 1, queue1.getEstimatedCount());

        manager.getTransactionManager().begin();

        Collection take = queue1.take(1);
        assertEquals("Evnt count", 1, take.size());

        Event event_copy = (Event) take.iterator().next();

        assertEquals("Reference equal", event, event_copy);
        manager.getTransactionManager().rollback();


        manager.getTransactionManager().begin();

        take = queue1.take(1);
        assertEquals("Evnt count", 1, take.size());
        event_copy = (Event) take.iterator().next();

        assertNotSame("Reference NOT equal", event, event_copy);
        manager.getTransactionManager().commit();
    }

    public void test_Take_interrupt_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();

        manager.getTransactionManager().begin();
        Event event = new Event("1");
        queue.put(Arrays.asList(new IEvent[]{event}));
        assertEquals("Queue size", 0, queue.size());
        assertEquals("Queue size", 1, queue.getEstimatedCount());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 1, queue.size());
        assertEquals("Queue size", 1, queue.getEstimatedCount());

        manager.getTransactionManager().begin();

        Collection take = queue.take(1);
        assertEquals("Evnt count", 1, take.size());

        Thread.currentThread().interrupt();
        manager.getTransactionManager().rollback();
    }


    public void testTxCallback() throws SinkException, SourceException {
        AbstractTransactionalMemoryQueue queue = createQueue();

        manager.getTransactionManager().begin();
        Event event = new Event("1");
        queue.put(Arrays.asList(new IEvent[]{event}));
        queue.getCurrentTx().registerCallback(new Tx.Callback() {
            public void beforeCompletion(boolean success) {
                assertTrue(success);
            }

            public void afterCompletion(boolean success) {
                assertTrue(success);
            }
        });
        assertEquals("Queue size", 0, queue.size());
        assertEquals("Queue size", 1, queue.getEstimatedCount());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 1, queue.size());
        assertEquals("Queue size", 1, queue.getEstimatedCount());

        manager.getTransactionManager().begin();

        Collection take = queue.take(1);
        assertEquals("Evnt count", 1, take.size());

        queue.getCurrentTx().registerCallback(new Tx.Callback() {
            public void beforeCompletion(boolean success) {
                assertFalse(success);
            }

            public void afterCompletion(boolean success) {
                assertFalse(success);
            }
        });

        manager.getTransactionManager().rollback();
    }

    public void testTxCallback_2() throws SinkException, SourceException {
        AbstractTransactionalMemoryQueue queue = createQueue();

        final AtomicLong beforeCounter = new AtomicLong(0l);
        final AtomicLong afterCounter = new AtomicLong(0l);
        manager.getTransactionManager().begin();
        Event event = new Event("1");
        queue.put(Arrays.asList(new IEvent[]{event}));

        queue.getCurrentTx().registerCallback(new Tx.Callback() {
            public void beforeCompletion(boolean success) {
                assertTrue(success);
                beforeCounter.incrementAndGet();
                throw new RuntimeException("Test with Exception");
            }

            public void afterCompletion(boolean success) {
                assertTrue(success);
                afterCounter.incrementAndGet();
                throw new RuntimeException("Test with Exception");
            }
        });
        queue.getCurrentTx().registerCallback(new Tx.Callback() {
            public void beforeCompletion(boolean success) {
                assertTrue(success);
                beforeCounter.incrementAndGet();
                throw new RuntimeException("Test with Exception");
            }

            public void afterCompletion(boolean success) {
                assertTrue(success);
                afterCounter.incrementAndGet();
                throw new RuntimeException("Test with Exception");
            }
        });
        assertEquals("Queue size", 0, queue.size());
        assertEquals("Queue size", 1, queue.getEstimatedCount());
        manager.getTransactionManager().commit();
        assertEquals("Queue size", 1, queue.size());
        assertEquals("Queue size", 1, queue.getEstimatedCount());

        assertEquals(beforeCounter.get(), 2l);
        assertEquals(afterCounter.get(), 2l);

    }

    protected TransactionalMemoryQueue createQueue() {
        return new TransactionalMemoryQueue(manager, ObjectCloneType.SERIALIZE, 10l);
    }
}
