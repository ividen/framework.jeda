package ru.kwanza.jeda.core.queue;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.api.internal.IQueueObserver;
import ru.kwanza.jeda.api.internal.SourceException;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import static ru.kwanza.jeda.api.IPriorityEvent.Priority;

/**
 * @author Guzanov Alexander
 */
public abstract class TestPriorityEventQueue extends TestCase {
    protected abstract IQueue createQueue();

    public void testAllPriority() throws SinkException, SourceException {
        IQueue queue = createQueue();
        queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("1", Priority.LOW)}));
        queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("1", Priority.NORMAL)}));
        queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("1", Priority.HIGH)}));
        queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("1", Priority.HIGHEST)}));
        queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("1", Priority.CRITICAL)}));

        Collection<PriorityEvent> events = queue.take(10);
        assertEquals("WrongSize", 5, events.size());
        Iterator<PriorityEvent> iterator = events.iterator();
        assertEquals("Wrong Priority", Priority.CRITICAL, iterator.next().getPriority());
        assertEquals("Wrong Priority", Priority.HIGHEST, iterator.next().getPriority());
        assertEquals("Wrong Priority", Priority.HIGH, iterator.next().getPriority());
        assertEquals("Wrong Priority", Priority.NORMAL, iterator.next().getPriority());
        assertEquals("Wrong Priority", Priority.LOW, iterator.next().getPriority());

        assertNull("Must be empty", queue.take(1));
    }

    public void testClogged() throws SinkException, SourceException {
        IQueue queue = createQueue();

        assertEquals(null, queue.tryPut(Arrays.asList(new IEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7"),
                new PriorityEvent("8"),
                new PriorityEvent("9"),
                new PriorityEvent("10")})));


        Collection collection = queue.tryPut(Arrays.asList(new IEvent[]{new PriorityEvent("1"),
                new PriorityEvent("11")}));

        assertEquals("Clogged size", 2, collection.size());

        assertEquals("Sink size", 10, queue.size());

        collection = queue.tryPut(Arrays.asList(new IEvent[]{new PriorityEvent("1"),
                new PriorityEvent("11"), new PriorityEvent("12"), new PriorityEvent("13"), new PriorityEvent("14")}));
        assertEquals("Clogged size", 5, collection.size());


        assertEquals("Sink size", 10, queue.size());

        int i = 0;
        Collection<PriorityEvent> events = queue.take(4);
        for (PriorityEvent e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }

        assertEquals("EventCollection size", 4, events.size());
        assertEquals("Sink size", 6, queue.size());


        collection = queue.tryPut(Arrays.asList(new IEvent[]{new PriorityEvent("1"),
                new PriorityEvent("11"), new PriorityEvent("12"), new PriorityEvent("13"), new PriorityEvent("14")}));
        assertEquals("Clogged size", 1, collection.size());


        assertEquals("Sink size", 10, queue.size());


        collection = queue.tryPut(Arrays.asList(new IEvent[]{new PriorityEvent("11"), new PriorityEvent("12"),
                new PriorityEvent("13"), new PriorityEvent("14")}));
        assertEquals("Clogged size", 4, collection.size());
        assertEquals("Sink size", 10, queue.size());
    }

    public void testEmptyPut() throws SinkException {
        IQueue queue = createQueue();
        assertEquals("Wrong queue size", 0, queue.size());
        queue.put(Arrays.asList(new IEvent[]{}));
        assertEquals("Wrong queue size", 0, queue.size());
    }

    public void testEmptyTryPut() throws SinkException {
        IQueue queue = createQueue();
        assertEquals("Wrong queue size", 0, queue.size());
        assertNull(queue.tryPut(Arrays.asList(new IEvent[]{})));
        assertEquals("Wrong queue size", 0, queue.size());
    }

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


        queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")}));


        try {
            queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("7"),
                    new PriorityEvent("7"),
                    new PriorityEvent("9"),
                    new PriorityEvent("10"),
                    new PriorityEvent("11")
            }));
        } catch (SinkException.Clogged e) {
        }

        assertEquals("Wrong decline size", 1, queue.tryPut(Arrays.asList(new IEvent[]{new PriorityEvent("7"),
                new PriorityEvent("7"),
                new PriorityEvent("9"),
                new PriorityEvent("10"),
                new PriorityEvent("11")
        })).size());


        assertEquals("Wrong count", 1, queue.take(1).size());
        assertEquals("Wrong count", 5, queue.take(5).size());
        assertEquals("Wrong count", 4, queue.take(10).size());


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

        try {
            queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("7"),
                    new PriorityEvent("7"),
                    new PriorityEvent("9"),
                    new PriorityEvent("10"),
                    new PriorityEvent("11")
            }));
        } catch (SinkException.Clogged e) {
        }

        assertEquals("Wrong count", 5, queue.take(10).size());

        assertEquals("Wrong count", 0, queueSize.size());
        assertEquals("Wrong count", 0, delta.size());
    }

    public void testPriorityPutClogged() throws SinkException, SourceException {
        IQueue queue = createQueue();

        assertEquals(null, queue.tryPut(Arrays.asList(new IEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6"),
                new PriorityEvent("7")
        })));


        Collection events = queue.tryPut(Arrays.asList(new IEvent[]{
                new PriorityEvent("11"),
                new PriorityEvent("12"),
                new PriorityEvent("13", Priority.LOW),
                new PriorityEvent("14", Priority.LOW),
                new PriorityEvent("15", Priority.HIGH),
                new PriorityEvent("16", Priority.CRITICAL),
                new PriorityEvent("17", Priority.CRITICAL)}));

        assertEquals("Clogged size", 4, events.size());
        assertEquals("Sink size", 10, queue.size());


        Iterator<PriorityEvent> iterator = events.iterator();
        PriorityEvent next = iterator.next();
        assertEquals("Priority wrong", Priority.NORMAL, next.getPriority());
        next = iterator.next();
        assertEquals("Priority wrong", Priority.NORMAL, next.getPriority());
        next = iterator.next();
        assertEquals("Priority wrong", Priority.LOW, next.getPriority());
        next = iterator.next();
        assertEquals("Priority wrong", Priority.LOW, next.getPriority());

        events = queue.take(4);
        iterator = events.iterator();
        next = iterator.next();
        assertEquals("Priority wrong", Priority.CRITICAL, next.getPriority());
        next = iterator.next();
        assertEquals("Priority wrong", Priority.CRITICAL, next.getPriority());
        next = iterator.next();
        assertEquals("Priority wrong", Priority.HIGH, next.getPriority());
        next = iterator.next();
        assertEquals("Priority wrong", Priority.NORMAL, next.getPriority());
    }

    public void testPutMany() throws SinkException, SourceException {
        IQueue queue = createQueue();
        queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")}));

        int i = 0;
        Collection<PriorityEvent> events = queue.take(1000);
        for (PriorityEvent e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }

        queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")}));

        i = 0;
        events = queue.take(1000);
        for (PriorityEvent e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }

        assertNull("No events", queue.take(1000));
    }

    public void testPutTake() throws SinkException, SourceException {
        IQueue queue = createQueue();
        passTake(queue);
    }

    public void testPutTake2Pass() throws SinkException, SourceException {
        IQueue queue = createQueue();
        passTake(queue);
        passTake(queue);
    }

    public void testPutTakeDiffPriority() throws SinkException, SourceException {
        IQueue queue = createQueue();
        queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("1", Priority.NORMAL),
                new PriorityEvent("2", Priority.LOW),
                new PriorityEvent("3", Priority.HIGH),
                new PriorityEvent("4", Priority.CRITICAL),
                new PriorityEvent("5", Priority.CRITICAL),
                new PriorityEvent("6", Priority.LOW)}));

        Collection<PriorityEvent> events = queue.take(2);
        Iterator<PriorityEvent> iterator = events.iterator();
        PriorityEvent next = iterator.next();
        assertEquals("Priority wrong", Priority.CRITICAL, next.getPriority());
        next = iterator.next();
        assertEquals("Priority wrong", Priority.CRITICAL, next.getPriority());

        assertEquals("Wrong return count", 2, events.size());


        events = queue.take(2);
        iterator = events.iterator();
        next = iterator.next();
        assertEquals("Priority wrong", Priority.HIGH, next.getPriority());
        next = iterator.next();
        assertEquals("Priority wrong", Priority.NORMAL, next.getPriority());
        assertEquals("Wrong return count", 2, events.size());

        events = queue.take(10);
        iterator = events.iterator();
        next = iterator.next();
        assertEquals("Priority wrong", Priority.LOW, next.getPriority());
        next = iterator.next();
        assertEquals("Priority wrong", Priority.LOW, next.getPriority());
        assertEquals("Wrong return count", 2, events.size());


        events = queue.take(10);
        assertNull("No events", events);

        //second pass

        queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("1", Priority.NORMAL),
                new PriorityEvent("2", Priority.LOW),
                new PriorityEvent("3", Priority.HIGH),
                new PriorityEvent("4", Priority.CRITICAL),
                new PriorityEvent("5", Priority.CRITICAL),
                new PriorityEvent("6", Priority.LOW)}));


        events = queue.take(2);
        iterator = events.iterator();
        next = iterator.next();
        assertEquals("Priority wrong", Priority.CRITICAL, next.getPriority());
        next = iterator.next();
        assertEquals("Priority wrong", Priority.CRITICAL, next.getPriority());

        assertEquals("Wrong return count", 2, events.size());


        events = queue.take(2);
        iterator = events.iterator();
        next = iterator.next();
        assertEquals("Priority wrong", Priority.HIGH, next.getPriority());
        next = iterator.next();
        assertEquals("Priority wrong", Priority.NORMAL, next.getPriority());
        assertEquals("Wrong return count", 2, events.size());

        events = queue.take(10);
        iterator = events.iterator();
        next = iterator.next();
        assertEquals("Priority wrong", Priority.LOW, next.getPriority());
        next = iterator.next();
        assertEquals("Priority wrong", Priority.LOW, next.getPriority());
        assertEquals("Wrong return count", 2, events.size());


        events = queue.take(10);
        assertNull("No events", events);
    }

    public void testSinkClosedException() {
        IQueue queue = createQueue();

        Thread.currentThread().interrupt();
        try {
            queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("1"),
                    new PriorityEvent("2"),
                    new PriorityEvent("3"),
                    new PriorityEvent("4"),
                    new PriorityEvent("5"),
                    new PriorityEvent("6")}));
            fail("Must be SourceException");
        } catch (SinkException e) {
        }
    }

    public void testSinkExceptionClogged() throws SinkException, SourceException {
        IQueue queue = createQueue();

        queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("1"),
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
            queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("1"),
                    new PriorityEvent("11")}));
            fail("Must be SinkException.Clogged");
        } catch (SinkException.Clogged e) {
        }

        assertEquals("Sink size", 10, queue.size());
        try {
            queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("1"),
                    new PriorityEvent("11"), new PriorityEvent("12"), new PriorityEvent("13"), new PriorityEvent("14")}));
            fail("Must be SinkException.Clogged");
        } catch (SinkException.Clogged e) {
        }

        assertEquals("Sink size", 10, queue.size());

        int i = 0;
        Collection<PriorityEvent> events = queue.take(4);
        for (PriorityEvent e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }

        assertEquals("EventCollection size", 4, events.size());
        assertEquals("Sink size", 6, queue.size());


        try {
            queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("1"),
                    new PriorityEvent("11"), new PriorityEvent("12"), new PriorityEvent("13"), new PriorityEvent("14")}));
            fail("Must be SinkException.Clogged");
        } catch (SinkException.Clogged e) {
        }

        assertEquals("Sink size", 6, queue.size());


        queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("11"), new PriorityEvent("12"), new PriorityEvent("13"),
                new PriorityEvent("14")}));

        assertEquals("Sink size", 10, queue.size());

        try {
            queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("11")}));
            fail("Must be SinkException.Clogged");
        } catch (SinkException.Clogged e) {
        }

        assertEquals("Sink size", 10, queue.size());
    }

    public void testSourceException() throws SinkException {
        IQueue queue = createQueue();
        queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")}));

        Thread.currentThread().interrupt();

        try {
            queue.take(100);
            fail("Must be SourceException");
        } catch (SourceException e) {
        }
    }

    public void testTryPutTake() throws SinkException, SourceException {
        IQueue queue = createQueue();
        tryPutTake(queue);
    }

    public void testTryPutTake2Pass() throws SinkException, SourceException {
        IQueue queue = createQueue();
        tryPutTake(queue);
        tryPutTake(queue);
    }

    private void passTake(IQueue queue) throws SinkException, SourceException {
        queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")}));
        assertEquals("WrongQueueSize", 6, queue.size());

        int i = 0;
        Collection<PriorityEvent> events = queue.take(1);
        for (PriorityEvent e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }
        assertEquals("WrongSize", 1, events.size());
        assertEquals("WrongQueueSize", 5, queue.size());

        events = queue.take(2);
        for (PriorityEvent e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }
        assertEquals("WrongSize", 2, events.size());
        assertEquals("WrongQueueSize", 3, queue.size());

        events = queue.take(3);
        for (PriorityEvent e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }
        assertEquals("WrongQueueSize", 0, queue.size());
        assertEquals("WrongSize", 3, events.size());

        events = queue.take(1000);
        assertNull("No events", events);
        events = queue.take(1000);
        assertNull("No events", events);

        queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")}));
        assertEquals("WrongQueueSize", 6, queue.size());

        events = queue.take(1000);
        i = 0;
        for (PriorityEvent e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }
    }

    private void tryPutTake(IQueue queue) throws SinkException, SourceException {
        assertEquals(null, queue.tryPut(Arrays.asList(new IEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")})));
        assertEquals("WrongQueueSize", 6, queue.size());

        int i = 0;
        Collection<PriorityEvent> events = queue.take(1);
        for (PriorityEvent e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }
        assertEquals("WrongSize", 1, events.size());
        assertEquals("WrongQueueSize", 5, queue.size());

        events = queue.take(2);
        for (PriorityEvent e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }
        assertEquals("WrongSize", 2, events.size());
        assertEquals("WrongQueueSize", 3, queue.size());

        events = queue.take(3);
        for (PriorityEvent e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }
        assertEquals("WrongQueueSize", 0, queue.size());
        assertEquals("WrongSize", 3, events.size());

        events = queue.take(1000);
        assertNull("No events", events);
        events = queue.take(1000);
        assertNull("No events", events);

        queue.put(Arrays.asList(new IEvent[]{new PriorityEvent("1"),
                new PriorityEvent("2"),
                new PriorityEvent("3"),
                new PriorityEvent("4"),
                new PriorityEvent("5"),
                new PriorityEvent("6")}));
        assertEquals("WrongQueueSize", 6, queue.size());

        events = queue.take(1000);
        i = 0;
        for (PriorityEvent e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }
    }
}

