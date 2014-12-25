package ru.kwanza.jeda.core.queue;

import ru.kwanza.jeda.api.*;
import ru.kwanza.jeda.api.internal.*;
import ru.kwanza.jeda.api.pushtimer.ITimer;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.api.internal.IQueueObserver;
import ru.kwanza.jeda.api.internal.SourceException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static junit.framework.Assert.*;

/**
 * @author Guzanov Alexander
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration("application-context-ds.xml")
public class TestTransactionalMemoryQueueWithDSTrx {
    @Autowired
    protected PlatformTransactionManager tm;

    @Mocked
    protected IJedaManager manager;

    @Before
    public void init(){
        new NonStrictExpectations(){{
            manager.getTransactionManager();result=tm;
        }};
    }

    @Test
    public void testClogged_Begin_Begin_Commit_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();

        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9")})));


        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

        assertEquals("Wrong clogged count!", 1, queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("11")})).size());

        assertEquals("Sink size", 0, queue.size());
        tm.commit(status2);
        assertEquals("Sink size", 1, queue.size());
        tm.commit(status1);
        assertEquals("Sink size", 10, queue.size());
    }

    @Test
    public void testClogged_Begin_Begin_Rollback_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();

        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9")})));


        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

        assertEquals("Wrong clogged count!", 1, queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("11")})).size());

        assertEquals("Sink size", 0, queue.size());
        tm.rollback(status2);
        assertEquals("Sink size", 0, queue.size());
        tm.commit(status1);
        assertEquals("Sink size", 9, queue.size());
    }

    @Test
    public void testClogged_Begin_Begin_Rollback_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();

        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9")})));


        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

        assertEquals("Wrong clogged count!", 1, queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("11")})).size());

        assertEquals("Sink size", 0, queue.size());
        tm.rollback(status2);
        assertEquals("Sink size", 0, queue.size());
        tm.rollback(status1);
        assertEquals("Sink size", 0, queue.size());
    }

    @Test
    public void testClogged_Begin_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();

        TransactionStatus status = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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
        tm.commit(status);
        assertEquals("Sink size", 10, queue.size());
    }

    @Test
    public void testClogged_Begin_Commit_Begin_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();

        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9")})));

        tm.commit(status1);
        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

        assertEquals("Wrong clogged count!", 1, queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("11")})).size());

        assertEquals("Sink size", 9, queue.size());
        tm.commit(status2);
        assertEquals("Sink size", 10, queue.size());
    }

    @Test
    public void testClogged_Begin_Rollback_Begin_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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


        tm.rollback(status1);
        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("11")})));

        assertEquals("Sink size", 0, queue.size());
        tm.commit(status2);
        assertEquals("Sink size", 2, queue.size());
    }

    @Test
    public void testClogged_Begin_Rollback_Begin_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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


        tm.rollback(status1);
        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("11")})));

        assertEquals("Sink size", 0, queue.size());
        tm.rollback(status2);
        assertEquals("Sink size", 0, queue.size());
    }

    @Test
    public void testEmptyPutCommit() throws SinkException {
        IQueue queue = createQueue();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
        TransactionStatus status = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue.put(Arrays.asList(new IEvent[]{}));
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
        tm.commit(status);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
    }

    @Test
    public void testEmptyPutRollback() throws SinkException {
        IQueue queue = createQueue();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
        TransactionStatus status = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue.put(Arrays.asList(new IEvent[]{}));
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
        tm.rollback(status);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
    }

    @Test
    public void testEmptyTryPutCommit() throws SinkException {
        IQueue queue = createQueue();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
        TransactionStatus status = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{})));
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
        tm.commit(status);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
    }

    @Test
    public void testEmptyTryPutRollback() throws SinkException {
        IQueue queue = createQueue();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
        TransactionStatus status = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{})));
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
        tm.rollback(status);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
    }

    @Test
    public void testMaxSize() {
        TransactionalMemoryQueue memoryQueue = new TransactionalMemoryQueue(manager);
        assertEquals("MaxSize wrong", Integer.MAX_VALUE, memoryQueue.getMaxSize());
    }

    @Test
    public void testObserver() throws SinkException, SourceException {
        IQueue queue = createQueue();
        final ArrayList<Integer> queueSize = new ArrayList<Integer>();
        final ArrayList<Integer> delta = new ArrayList<Integer>();
        queue.setObserver(new IQueueObserver() {
            public void notifyChange(int s, int d) {
                queueSize.add(s);
                delta.add(d);
            }
        });

        assertNotNull("Must be NOT null", queue.getObserver());

        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));
        tm.commit(status1);

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        try {
            queue.put(Arrays.asList(new IEvent[]{new Event("7"),
                    new Event("7"),
                    new Event("9"),
                    new Event("10"),
                    new Event("11")
            }));
        } catch (SinkException.Clogged e) {
        }
        tm.commit(status2);


        TransactionStatus status3 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertEquals("Wrong decline size", 1, queue.tryPut(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("7"),
                new Event("9"),
                new Event("10"),
                new Event("11")
        })).size());
        tm.commit(status3);

        TransactionStatus status4 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertEquals("Wrong count", 1, queue.take(1).size());
        tm.commit(status4);

        TransactionStatus status5 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertEquals("Wrong count", 5, queue.take(5).size());
        tm.commit(status5);

        TransactionStatus status6 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertEquals("Wrong count", 4, queue.take(10).size());
        tm.commit(status6);


        assertEquals("Wrong notify size for 1 put", Integer.valueOf(6), queueSize.get(0));
        assertEquals("Wrong notify size for 2 put", Integer.valueOf(10), queueSize.get(1));
        assertEquals("Wrong notify size for 1 take", Integer.valueOf(9), queueSize.get(2));
        assertEquals("Wrong notify size for 2 take", Integer.valueOf(4), queueSize.get(3));
        assertEquals("Wrong notify size for 3 take", Integer.valueOf(0), queueSize.get(4));

        assertEquals("Wrong notify delta for 1 put", Integer.valueOf(6), delta.get(0));
        assertEquals("Wrong notify delta for 2 put", Integer.valueOf(4), delta.get(1));
        assertEquals("Wrong notify delta for 1 take", Integer.valueOf(-1), delta.get(2));
        assertEquals("Wrong notify delta for 2 take", Integer.valueOf(-5), delta.get(3));
        assertEquals("Wrong notify delta for 3 take", Integer.valueOf(-4), delta.get(4));

        queue.setObserver(null);
        queueSize.clear();
        delta.clear();


        assertNull("Must be null", queue.getObserver());

        TransactionStatus status7 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        try {
            queue.put(Arrays.asList(new IEvent[]{new Event("7"),
                    new Event("7"),
                    new Event("9"),
                    new Event("10"),
                    new Event("11")
            }));
        } catch (SinkException.Clogged e) {
        }
        tm.commit(status7);

        TransactionStatus status8 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertEquals("Wrong count", 5, queue.take(10).size());
        tm.commit(status8);

        assertEquals("Wrong count", 0, queueSize.size());
        assertEquals("Wrong count", 0, delta.size());
    }

    @Test
    public void testPutCommit() throws SinkException, SourceException {
        IQueue<Event> queue = createQueue();
        TransactionStatus status9 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue.put(Arrays.asList(new Event[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());
        tm.commit(status9);
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

    @Test
    public void testPutPutCommit() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        TransactionStatus status = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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
        tm.commit(status);
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

    @Test
    public void testPutPutRollback() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        TransactionStatus status = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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
        tm.rollback(status);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
    }

    @Test
    public void testPutRollback() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        TransactionStatus status = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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
        tm.rollback(status);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
    }

    @Test
    public void testPut_Begin_Begin_Commit_Commit() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue.put(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        tm.commit(status2);
        assertEquals("Wrong queue size", 4, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());

        tm.commit(status1);
        assertEquals("Wrong queue size", 10, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
    }

    @Test
    public void testPut_Begin_Begin_Rollback_Commit() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue.put(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        tm.rollback(status2);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        tm.commit(status1);
        assertEquals("Wrong queue size", 6, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());
    }

    @Test
    public void testPut_Begin_Begin_Rollback_Rollback() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue.put(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        tm.rollback(status2);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        tm.rollback(status1);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
    }

    @Test
    public void testPut_Begin_Commit_Begin_Commit() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());
        tm.commit(status1);
        assertEquals("Wrong queue size", 6, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue.put(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));

        assertEquals("Wrong queue size", 6, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        tm.commit(status2);
        assertEquals("Wrong queue size", 10, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
    }

    @Test
    public void testPut_Begin_Rollback_Begin_Commit() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());
        tm.rollback(status1);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue.put(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 4, queue.getEstimatedCount());
        tm.commit(status2);
        assertEquals("Wrong queue size", 4, queue.size());
        assertEquals("WrongEstimateQueueSize", 4, queue.getEstimatedCount());
    }

    @Test
    public void testPut_Begin_Rollback_Begin_Rollback() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());
        tm.rollback(status1);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue.put(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 4, queue.getEstimatedCount());
        tm.rollback(status2);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
    }

    @Test
    public void testSinkExceptionClogged_Begin_Begin_Commit_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();

        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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
        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        try {
            queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                    new Event("11")}));
            fail("Must be SinkException.Clogged");
        } catch (SinkException.Clogged e) {
        }
        assertEquals("Sink size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        tm.commit(status2);
        assertEquals("Sink size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());

        tm.commit(status1);
        assertEquals("Sink size", 10, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
    }

    @Test
    public void testSinkExceptionClogged_Begin_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();

        TransactionStatus status = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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
        tm.commit(status);
        assertEquals("Sink size", 10, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
    }

    @Test
    public void testSinkExceptionClogged_Begin_Commit_Begin_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();

        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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

        tm.commit(status1);
        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        try {
            queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                    new Event("11")}));
            fail("Must be SinkException.Clogged");
        } catch (SinkException.Clogged e) {
        }

        assertEquals("Sink size", 10, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        tm.commit(status2);
        assertEquals("Sink size", 10, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
    }

    @Test
    public void testSinkExceptionClogged_Begin_Rollback_Begin_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();

        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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
        tm.rollback(status1);
        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        try {
            queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                    new Event("11")}));
        } catch (SinkException.Clogged e) {
            fail("Must NOT  be SinkException.Clogged");
        }
        assertEquals("Sink size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 2, queue.getEstimatedCount());
        tm.commit(status2);
        assertEquals("Sink size", 2, queue.size());
        assertEquals("WrongEstimateQueueSize", 2, queue.getEstimatedCount());
    }

    @Test
    public void testSinkExceptionClogged_Begin_Rollback_Begin_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();

        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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
        tm.rollback(status1);
        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        try {
            queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                    new Event("11")}));
        } catch (SinkException.Clogged e) {
            fail("Must NOT  be SinkException.Clogged");
        }
        assertEquals("Sink size", 0, queue.size());
        tm.rollback(status2);
        assertEquals("Sink size", 0, queue.size());
    }

    @Test
    public void testTakeBegin_Begin_Commit_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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
        tm.commit(status1);

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Collection c = queue.take(5);
        assertEquals("Elemets size", 5, c.size());
        assertEquals("Queue size", 10, queue.size());


        TransactionStatus status3 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        c = queue.take(4);
        assertEquals("Elemets size", 4, c.size());
        assertEquals("Queue size", 10, queue.size());
        tm.commit(status3);
        assertEquals("Qeuue size", 6, queue.size());
        tm.commit(status2);
        assertEquals("Qeuue size", 1, queue.size());
    }

    @Test
    public void testTakeBegin_Begin_Rollback_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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
        tm.commit(status1);

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Collection c = queue.take(5);
        assertEquals("Elemets size", 5, c.size());
        assertEquals("Queue size", 10, queue.size());


        TransactionStatus status3 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        c = queue.take(4);
        assertEquals("Elemets size", 4, c.size());
        assertEquals("Queue size", 10, queue.size());
        tm.rollback(status3);
        assertEquals("Qeuue size", 10, queue.size());
        tm.commit(status2);
        assertEquals("Qeuue size", 5, queue.size());
    }

    @Test
    public void testTakeBegin_Begin_Rollback_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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
        tm.commit(status1);

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Collection c = queue.take(5);
        assertEquals("Elemets size", 5, c.size());
        assertEquals("Queue size", 10, queue.size());


        TransactionStatus status3 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        c = queue.take(4);
        assertEquals("Elemets size", 4, c.size());
        assertEquals("Queue size", 10, queue.size());
        tm.rollback(status3);
        assertEquals("Qeuue size", 10, queue.size());
        tm.rollback(status2);
        assertEquals("Qeuue size", 10, queue.size());
    }

    @Test
    public void testTakeBegin_Commit_Begin_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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
        tm.commit(status1);

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Collection c = queue.take(5);
        assertEquals("Elemets size", 5, c.size());
        assertEquals("Queue size", 10, queue.size());
        tm.commit(status2);
        assertEquals("Queue size", 5, queue.size());

        TransactionStatus status3 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        c = queue.take(4);
        assertEquals("Elemets size", 4, c.size());
        assertEquals("Queue size", 5, queue.size());
        tm.commit(status3);
        assertEquals("Qeuue size", 1, queue.size());
    }

    @Test
    public void testTakeBegin_Rollback_Begin_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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
        tm.commit(status1);

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Collection c = queue.take(5);
        assertEquals("Elemets size", 5, c.size());
        assertEquals("Queue size", 10, queue.size());
        tm.rollback(status2);
        assertEquals("Queue size", 10, queue.size());

        TransactionStatus status3 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        c = queue.take(4);
        assertEquals("Elemets size", 4, c.size());
        assertEquals("Queue size", 10, queue.size());
        tm.commit(status3);
        assertEquals("Qeuue size", 6, queue.size());
    }

    @Test
    public void testTakeBegin_Rollback_Begin_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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
        tm.commit(status1);

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Collection c = queue.take(5);
        assertEquals("Elemets size", 5, c.size());
        assertEquals("Queue size", 10, queue.size());
        tm.rollback(status2);
        assertEquals("Queue size", 10, queue.size());

        TransactionStatus status3 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        c = queue.take(4);
        assertEquals("Elemets size", 4, c.size());
        assertEquals("Queue size", 10, queue.size());
        tm.rollback(status3);
        assertEquals("Qeuue size", 10, queue.size());
    }

    @Test
    public void testTakeCommit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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
        tm.commit(status1);

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Collection c = queue.take(100);
        assertEquals("Elemets size", 10, c.size());
        assertEquals("Queue size", 10, queue.size());
        tm.commit(status2);
        assertEquals("Qeuue size", 0, queue.size());
    }

    @Test
    public void testTakeRollback() throws SinkException, SourceException {
        IQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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
        tm.commit(status1);

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Collection c = queue.take(100);
        assertEquals("Elemets size", 10, c.size());
        assertEquals("Queue size", 10, queue.size());
        tm.rollback(status2);
        assertEquals("Qeuue size", 10, queue.size());
    }

    @Test
    public void testTakeTakeCommit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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
        tm.commit(status1);

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Collection c = queue.take(5);
        assertEquals("Elemets size", 5, c.size());
        assertEquals("Queue size", 10, queue.size());
        c = queue.take(4);
        assertEquals("Elemets size", 4, c.size());
        assertEquals("Queue size", 10, queue.size());
        tm.commit(status2);
        assertEquals("Qeuue size", 1, queue.size());
    }

    @Test
    public void testTakeTakeRollback() throws SinkException, SourceException {
        IQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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
        tm.commit(status1);

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Collection c = queue.take(5);
        assertEquals("Elemets size", 5, c.size());
        assertEquals("Queue size", 10, queue.size());
        c = queue.take(4);
        assertEquals("Elemets size", 4, c.size());
        assertEquals("Queue size", 10, queue.size());
        tm.rollback(status2);
        assertEquals("Qeuue size", 10, queue.size());
    }

    @Test
    public void testTakeZero() throws SourceException, SinkException {
        IQueue queue = createQueue();

        assertEquals("WrongQueueSize", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));
        assertEquals("WrongQueueSize", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());
        tm.commit(status1);

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertNull("Zero take ", queue.take(0));
        assertEquals("WrongQueueSize", 6, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());
        tm.commit(status2);

        assertEquals("WrongQueueSize", 6, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());
    }

    @Test
    public void testTryPutCommit() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        TransactionStatus status = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());
        tm.commit(status);
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

    @Test
    public void testTryPutInterrupt() throws SourceException, SinkException {
        IQueue queue = createQueue();

        assertEquals("WrongQueueSize", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
        TransactionStatus status = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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
                tm.commit(status);
            } catch (SinkException.Closed e) {
                assertEquals("WrongQueueSize", 0, queue.size());
                assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
                tm.rollback(status);
            }
        } catch (Throwable e) {
            assertEquals("WrongQueueSize", 0, queue.size());
            assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
            tm.rollback(status);
        }

        assertEquals("WrongQueueSize", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
    }

    @Test
    public void testTryPutPutCommit() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        TransactionStatus status = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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
        tm.commit(status);
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

    @Test
    public void testTryPutPutRollback() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        TransactionStatus status = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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
        tm.rollback(status);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
    }

    @Test
    public void testTryPutRollback() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        TransactionStatus status = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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
        tm.rollback(status);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
    }

    @Test
    public void testTryPut_Begin_Begin_Commit_Commit() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        tm.commit(status2);
        assertEquals("Wrong queue size", 4, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());

        tm.commit(status1);
        assertEquals("Wrong queue size", 10, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
    }

    @Test
    public void testTryPut_Begin_Begin_Rollback_Commit() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        tm.rollback(status2);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        tm.commit(status1);
        assertEquals("Wrong queue size", 6, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());
    }

    @Test
    public void testTryPut_Begin_Begin_Rollback_Rollback() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        tm.rollback(status2);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        tm.rollback(status1);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
    }

    @Test
    public void testTryPut_Begin_Commit_Begin_Commit() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());
        tm.commit(status1);
        assertEquals("Wrong queue size", 6, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")})));

        assertEquals("Wrong queue size", 6, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
        tm.commit(status2);
        assertEquals("Wrong queue size", 10, queue.size());
        assertEquals("WrongEstimateQueueSize", 10, queue.getEstimatedCount());
    }

    @Test
    public void testTryPut_Begin_Rollback_Begin_Commit() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());
        tm.rollback(status1);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 4, queue.getEstimatedCount());
        tm.commit(status2);
        assertEquals("Wrong queue size", 4, queue.size());
        assertEquals("WrongEstimateQueueSize", 4, queue.getEstimatedCount());
    }

    @Test
    public void testTryPut_Begin_Rollback_Begin_Rollback() throws SinkException, SourceException {
        TransactionalMemoryQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());
        tm.rollback(status1);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertNull("Not clogged!", queue.tryPut(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")})));

        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 4, queue.getEstimatedCount());
        tm.rollback(status2);
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongEstimateQueueSize", 0, queue.getEstimatedCount());
    }

    @Test
    public void test_2_Queue_Put_Commit() throws SinkException, SourceException {
        IQueue queue1 = createQueue();
        IQueue queue2 = createQueue();
        TransactionStatus status = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue1.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5")}));
        queue2.put(Arrays.asList(new IEvent[]{new Event("6"),
                new Event("7")}));
        assertEquals("Queue size", 0, queue1.size());
        assertEquals("Queue size", 0, queue2.size());
        tm.commit(status);
        assertEquals("Queue size", 5, queue1.size());
        assertEquals("Queue size", 2, queue2.size());
    }

    @Test
    public void test_2_Queue_Put_Rollback() throws SinkException, SourceException {
        IQueue queue1 = createQueue();
        IQueue queue2 = createQueue();
        TransactionStatus status = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue1.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5")}));
        queue2.put(Arrays.asList(new IEvent[]{new Event("6"),
                new Event("7")}));
        assertEquals("Queue size", 0, queue1.size());
        assertEquals("Queue size", 0, queue2.size());
        tm.rollback(status);
        assertEquals("Queue size", 0, queue1.size());
        assertEquals("Queue size", 0, queue2.size());
    }

    @Test
    public void test_2_Queue_Take_Commit() throws SinkException, SourceException {
        IQueue queue1 = createQueue();
        IQueue queue2 = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue1.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5")}));
        queue2.put(Arrays.asList(new IEvent[]{new Event("6"),
                new Event("7")}));
        assertEquals("Queue size", 0, queue1.size());
        assertEquals("Queue size", 0, queue2.size());
        tm.commit(status1);
        assertEquals("Queue size", 5, queue1.size());
        assertEquals("Queue size", 2, queue2.size());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

        assertEquals("Evnt count", 3, queue1.take(3).size());
        assertEquals("Evnt count", 1, queue2.take(1).size());

        assertEquals("Queue size", 5, queue1.size());
        assertEquals("Queue size", 2, queue2.size());
        tm.commit(status2);
        assertEquals("Queue size", 2, queue1.size());
        assertEquals("Queue size", 1, queue2.size());
    }

    @Test
    public void test_2_Queue_Take_Rollback() throws SinkException, SourceException {
        IQueue queue1 = createQueue();
        IQueue queue2 = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue1.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5")}));
        queue2.put(Arrays.asList(new IEvent[]{new Event("6"),
                new Event("7")}));
        assertEquals("Queue size", 0, queue1.size());
        assertEquals("Queue size", 0, queue2.size());
        tm.commit(status1);
        assertEquals("Queue size", 5, queue1.size());
        assertEquals("Queue size", 2, queue2.size());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

        assertEquals("Evnt count", 3, queue1.take(3).size());
        assertEquals("Evnt count", 1, queue2.take(1).size());

        assertEquals("Queue size", 5, queue1.size());
        assertEquals("Queue size", 2, queue2.size());
        tm.rollback(status2);
        assertEquals("Queue size", 5, queue1.size());
        assertEquals("Queue size", 2, queue2.size());
    }

    @Test
    public void test_Begin_Put_Begin_Take_Commit_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Collection c = queue.take(100);
        assertNull("Must be empty", c);
        assertEquals("Queue size", 0, queue.size());
        tm.commit(status2);
        assertEquals("Qeuue size", 0, queue.size());
        tm.commit(status1);
        assertEquals("Qeuue size", 10, queue.size());
    }

    @Test
    public void test_Begin_Put_Begin_Take_Rollback_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Collection c = queue.take(100);
        assertNull("Must be empty", c);
        assertEquals("Queue size", 0, queue.size());
        tm.rollback(status2);
        assertEquals("Qeuue size", 0, queue.size());
        tm.commit(status1);
        assertEquals("Qeuue size", 10, queue.size());
    }

    @Test
    public void test_Begin_Put_Begin_Take_Rollback_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Collection c = queue.take(100);
        assertNull("Must be empty", c);
        assertEquals("Queue size", 0, queue.size());
        tm.rollback(status2);
        assertEquals("Qeuue size", 0, queue.size());
        tm.rollback(status1);
        assertEquals("Qeuue size", 0, queue.size());
    }

    @Test
    public void test_Begin_Put_Commit_Begin_Take_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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
        tm.commit(status1);
        assertEquals("Queue size", 10, queue.size());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Collection c = queue.take(100);
        assertEquals("Must be empty", 10, c.size());
        assertEquals("Queue size", 10, queue.size());
        tm.commit(status2);
        assertEquals("Qeuue size", 0, queue.size());
    }

    @Test
    public void test_Begin_Put_Commit_Begin_Take_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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
        tm.commit(status1);
        assertEquals("Queue size", 10, queue.size());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Collection c = queue.take(100);
        assertEquals("Must Not be empty", 10, c.size());
        assertEquals("Queue size", 10, queue.size());
        tm.rollback(status2);
        assertEquals("Qeuue size", 10, queue.size());
    }

    @Test
    public void test_Begin_Put_Rollback_Begin_Take_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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
        tm.rollback(status1);
        assertEquals("Queue size", 0, queue.size());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Collection c = queue.take(100);
        assertNull("Must be empty", c);
        assertEquals("Queue size", 0, queue.size());
        tm.commit(status2);
        assertEquals("Qeuue size", 0, queue.size());
    }

    @Test
    public void test_Begin_Put_Rollback_Begin_Take_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
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
        tm.rollback(status1);
        assertEquals("Queue size", 0, queue.size());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Collection c = queue.take(100);
        assertNull("Must be empty", c);
        assertEquals("Queue size", 0, queue.size());
        tm.rollback(status2);
        assertEquals("Qeuue size", 0, queue.size());
    }

    @Test
    public void test_Begin_Take_Begin_Put_Commit_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5")}));
        tm.commit(status1);
        assertEquals("Queue size", 5, queue.size());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Collection c = queue.take(2);
        assertEquals("Must be empty", 2, c.size());
        assertEquals("Queue size", 5, queue.size());

        TransactionStatus status3 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertEquals("Clogged Size : ", 1, queue.tryPut(Arrays.asList(new IEvent[]{new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10"), new Event("11")})).size());

        assertEquals("Queue size", 5, queue.size());
        tm.commit(status3);
        assertEquals("Queue size", 10, queue.size());
        tm.commit(status2);
        assertEquals("Queue size", 8, queue.size());
    }

    @Test
    public void test_Begin_Take_Begin_Put_Commit_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5")}));
        tm.commit(status1);
        assertEquals("Queue size", 5, queue.size());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Collection c = queue.take(2);
        assertEquals("Must be empty", 2, c.size());
        assertEquals("Queue size", 5, queue.size());

        TransactionStatus status3 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertEquals("Clogged Size : ", 1, queue.tryPut(Arrays.asList(new IEvent[]{new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10"), new Event("11")})).size());

        assertEquals("Queue size", 5, queue.size());
        tm.commit(status3);
        assertEquals("Queue size", 10, queue.size());
        tm.rollback(status2);
        assertEquals("Queue size", 10, queue.size());
    }

    @Test
    public void test_Begin_Take_Begin_Put_Rollback_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5")}));
        tm.commit(status1);
        assertEquals("Queue size", 5, queue.size());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Collection c = queue.take(2);
        assertEquals("Must be empty", 2, c.size());
        assertEquals("Queue size", 5, queue.size());

        TransactionStatus status3 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertEquals("Clogged Size : ", 1, queue.tryPut(Arrays.asList(new IEvent[]{new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10"), new Event("11")})).size());

        assertEquals("Queue size", 5, queue.size());
        tm.rollback(status3);
        assertEquals("Queue size", 5, queue.size());
        tm.commit(status2);
        assertEquals("Queue size", 3, queue.size());
    }

    @Test
    public void test_Begin_Take_Begin_Put_Rollback_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5")}));
        tm.commit(status1);
        assertEquals("Queue size", 5, queue.size());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Collection c = queue.take(2);
        assertEquals("Must be empty", 2, c.size());
        assertEquals("Queue size", 5, queue.size());

        TransactionStatus status3 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertEquals("Clogged Size : ", 1, queue.tryPut(Arrays.asList(new IEvent[]{new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10"), new Event("11")})).size());

        assertEquals("Queue size", 5, queue.size());
        tm.rollback(status3);
        assertEquals("Queue size", 5, queue.size());
        tm.rollback(status2);
        assertEquals("Queue size", 5, queue.size());
    }

    @Test
    public void test_Begin_Take_Commit_Begin_Put_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5")}));
        tm.commit(status1);
        assertEquals("Queue size", 5, queue.size());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Collection c = queue.take(2);
        assertEquals("Must be empty", 2, c.size());
        assertEquals("Queue size", 5, queue.size());
        tm.commit(status2);
        assertEquals("Qeuue size", 3, queue.size());

        TransactionStatus status3 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue.put(Arrays.asList(new IEvent[]{new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));
        assertEquals("Queue size", 3, queue.size());
        tm.commit(status3);
        assertEquals("Queue size", 8, queue.size());
    }

    @Test
    public void test_Begin_Take_Rollback_Begin_Put_Commit() throws SinkException, SourceException {
        IQueue queue = createQueue();
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5")}));
        tm.commit(status1);
        assertEquals("Queue size", 5, queue.size());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Collection c = queue.take(2);
        assertEquals("Must be empty", 2, c.size());
        assertEquals("Queue size", 5, queue.size());
        tm.rollback(status2);
        assertEquals("Qeuue size", 5, queue.size());

        TransactionStatus status3 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        queue.put(Arrays.asList(new IEvent[]{new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")}));
        assertEquals("Queue size", 5, queue.size());
        tm.commit(status3);
        assertEquals("Queue size", 10, queue.size());
    }

    @Test
    public void test_Clone_Copy() throws SinkException, SourceException {
        IQueue queue1 = new TransactionalMemoryQueue(manager, ObjectCloneType.CLONE, 10);

        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Event event = new Event("1");
        queue1.put(Arrays.asList(new IEvent[]{event}));
        assertEquals("Queue size", 0, queue1.size());
        tm.commit(status1);
        assertEquals("Queue size", 1, queue1.size());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

        Collection take = queue1.take(1);
        assertEquals("Evnt count", 1, take.size());

        Event event_copy = (Event) take.iterator().next();

        assertEquals("Reference equal", event, event_copy);
        tm.commit(status2);
    }

    @Test
    public void test_Non_Clone_Copy() throws SinkException, SourceException {
        IQueue queue1 = new TransactionalMemoryQueue(manager, ObjectCloneType.CLONE, 10);

        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        NonSerializableEvent event = new NonSerializableEvent("1");
        queue1.put(Arrays.asList(new IEvent[]{event}));
        assertEquals("Queue size", 0, queue1.size());
        tm.commit(status1);
        assertEquals("Queue size", 1, queue1.size());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

        Collection take = queue1.take(1);
        assertEquals("Evnt count", 1, take.size());

        NonSerializableEvent event_copy = (NonSerializableEvent) take.iterator().next();

        assertTrue("Reference equal", (event == event_copy));
        tm.commit(status2);
    }

    @Test
    public void test_Non_Copy() throws SinkException, SourceException {
        IQueue queue1 = new TransactionalMemoryQueue(manager, ObjectCloneType.NONE, 10);

        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        NonSerializableEvent event = new NonSerializableEvent("1");
        queue1.put(Arrays.asList(new IEvent[]{event}));
        assertEquals("Queue size", 0, queue1.size());
        tm.commit(status1);
        assertEquals("Queue size", 1, queue1.size());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

        Collection take = queue1.take(1);
        assertEquals("Evnt count", 1, take.size());

        NonSerializableEvent event_copy = (NonSerializableEvent) take.iterator().next();

        assertTrue("Reference equal", (event == event_copy));
        tm.commit(status2);
    }

    @Test
    public void test_Non_Serialization_Copy() throws SinkException, SourceException {
        IQueue queue1 = createQueue();

        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        NonSerializableEvent event = new NonSerializableEvent("1");
        queue1.put(Arrays.asList(new IEvent[]{event}));
        assertEquals("Queue size", 0, queue1.size());
        tm.commit(status1);
        assertEquals("Queue size", 1, queue1.size());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

        Collection take = queue1.take(1);
        assertEquals("Evnt count", 1, take.size());

        tm.rollback(status2);

        TransactionStatus status3 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

        take = queue1.take(1);
        assertEquals("Evnt count", 1, take.size());

        tm.commit(status3);

        NonSerializableEvent event_copy = (NonSerializableEvent) take.iterator().next();

        assertTrue("Reference equal", (event == event_copy));
    }

    @Test
    public void test_Put_interrupt_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();

        TransactionStatus status = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Event event = new Event("1");
        queue.put(Arrays.asList(new IEvent[]{event}));
        assertEquals("Queue size", 0, queue.size());
        assertEquals("Queue size", 1, queue.getEstimatedCount());
        Thread.currentThread().interrupt();
        tm.commit(status);
        assertEquals("Queue size", 0, queue.size());
        assertEquals("Queue size", 1, queue.getEstimatedCount());
    }

    @Test
    public void test_Serialization_Copy() throws SinkException, SourceException {
        IQueue queue1 = createQueue();

        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Event event = new Event("1");
        queue1.put(Arrays.asList(new IEvent[]{event}));
        assertEquals("Queue size", 0, queue1.size());
        tm.commit(status1);
        assertEquals("Queue size", 1, queue1.size());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Collection take = queue1.take(1);
        assertEquals("Evnt count", 1, take.size());
        tm.rollback(status2);

        TransactionStatus status3 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        take = queue1.take(1);
        assertEquals("Evnt count", 1, take.size());
        Event event_copy = (Event) take.iterator().next();
        tm.commit(status3);

        assertNotSame("Reference must not be equal", event, event_copy);
        assertEquals("Objects equals", event, event_copy);
    }

    @Test
    public void test_TakeCLONE_Rollback() throws SinkException, SourceException {
        IQueue queue1 = new TransactionalMemoryQueue(manager, ObjectCloneType.CLONE, 10);

        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Event event = new Event("1");
        queue1.put(Arrays.asList(new IEvent[]{event}));
        assertEquals("Queue size", 0, queue1.size());
        assertEquals("Queue size", 1, queue1.getEstimatedCount());
        tm.commit(status1);
        assertEquals("Queue size", 1, queue1.size());
        assertEquals("Queue size", 1, queue1.getEstimatedCount());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

        Collection take = queue1.take(1);
        assertEquals("Evnt count", 1, take.size());

        Event event_copy = (Event) take.iterator().next();

        assertEquals("Reference equal", event, event_copy);
        tm.rollback(status2);


        TransactionStatus status3 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

        take = queue1.take(1);
        assertEquals("Evnt count", 1, take.size());
        event_copy = (Event) take.iterator().next();

        assertNotSame("Reference NOT equal", event, event_copy);
        tm.commit(status3);
    }

    @Test
    public void test_Take_interrupt_Rollback() throws SinkException, SourceException {
        IQueue queue = createQueue();

        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Event event = new Event("1");
        queue.put(Arrays.asList(new IEvent[]{event}));
        assertEquals("Queue size", 0, queue.size());
        assertEquals("Queue size", 1, queue.getEstimatedCount());
        tm.commit(status1);
        assertEquals("Queue size", 1, queue.size());
        assertEquals("Queue size", 1, queue.getEstimatedCount());

        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

        Collection take = queue.take(1);
        assertEquals("Evnt count", 1, take.size());

        Thread.currentThread().interrupt();
        tm.rollback(status2);
    }

    protected TransactionalMemoryQueue createQueue() {
        return new TransactionalMemoryQueue(manager, ObjectCloneType.SERIALIZE, 10);
    }
}
