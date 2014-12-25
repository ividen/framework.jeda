package ru.kwanza.jeda.core.queue;

import org.junit.Test;
import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.api.internal.IQueueObserver;
import ru.kwanza.jeda.api.internal.SourceException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static junit.framework.Assert.*;


public abstract class TestEventQueue{
    protected abstract IQueue createQueue();

    @Test
    public void testClogged() throws SinkException, SourceException {
        IQueue queue = createQueue();

        assertEquals(null, queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6"),
                new Event("7"),
                new Event("8"),
                new Event("9"),
                new Event("10")})));


        Collection collection = queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("11")}));

        assertEquals("Clogged size", 2, collection.size());

        assertEquals("Sink size", 10, queue.size());
        assertEquals("WrongQueueEstimateSize", 10, queue.getEstimatedCount());

        collection = queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("11"), new Event("12"), new Event("13"), new Event("14")}));
        assertEquals("Clogged size", 5, collection.size());


        assertEquals("Sink size", 10, queue.size());
        assertEquals("WrongQueueEstimateSize", 10, queue.getEstimatedCount());

        int i = 0;
        Collection<Event> events = queue.take(4);
        for (Event e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }

        assertEquals("EventCollection size", 4, events.size());
        assertEquals("Sink size", 6, queue.size());
        assertEquals("WrongQueueEstimateSize", 6, queue.getEstimatedCount());


        collection = queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("11"), new Event("12"), new Event("13"), new Event("14")}));
        assertEquals("Clogged size", 1, collection.size());


        assertEquals("Sink size", 10, queue.size());
        assertEquals("WrongQueueEstimateSize", 10, queue.getEstimatedCount());


        collection = queue.tryPut(Arrays.asList(new IEvent[]{new Event("11"), new Event("12"), new Event("13"), new Event("14")}));
        assertEquals("Clogged size", 4, collection.size());
        assertEquals("Sink size", 10, queue.size());
        assertEquals("WrongQueueEstimateSize", 10, queue.getEstimatedCount());
    }

    @Test
    public void testEmptyPut() throws SinkException {
        IQueue queue = createQueue();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongQueueEstimateSize", 0, queue.getEstimatedCount());
        queue.put(Arrays.asList(new IEvent[]{}));
        assertEquals("Wrong queue size", 0, queue.size());
    }

    @Test
    public void testEmptyTryPut() throws SinkException {
        IQueue queue = createQueue();
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongQueueEstimateSize", 0, queue.getEstimatedCount());
        assertNull(queue.tryPut(Arrays.asList(new IEvent[]{})));
        assertEquals("Wrong queue size", 0, queue.size());
        assertEquals("WrongQueueEstimateSize", 0, queue.getEstimatedCount());
    }

    @Test
    public void testObserving() throws SinkException, SourceException {
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


        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));


        try {
            queue.put(Arrays.asList(new IEvent[]{new Event("7"),
                    new Event("7"),
                    new Event("9"),
                    new Event("10"),
                    new Event("11")
            }));
        } catch (SinkException.Clogged e) {
        }

        assertEquals("Wrong decline size", 1, queue.tryPut(Arrays.asList(new IEvent[]{new Event("7"),
                new Event("7"),
                new Event("9"),
                new Event("10"),
                new Event("11")
        })).size());


        assertEquals("Wrong count", 1, queue.take(1).size());
        assertEquals("Wrong count", 5, queue.take(5).size());
        assertEquals("Wrong count", 4, queue.take(10).size());


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

        try {
            queue.put(Arrays.asList(new IEvent[]{new Event("7"),
                    new Event("7"),
                    new Event("9"),
                    new Event("10"),
                    new Event("11")
            }));
        } catch (SinkException.Clogged e) {
        }

        assertEquals("Wrong count", 5, queue.take(100).size());

        assertEquals("Wrong count", 0, queueSize.size());
        assertEquals("Wrong count", 0, delta.size());
    }

    @Test
    public void testPutMany() throws SinkException, SourceException {
        IQueue queue = createQueue();
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));

        int i = 0;
        Collection<Event> events = queue.take(1000);
        for (Event e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }

        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));

        i = 0;
        events = queue.take(1000);
        for (Event e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }

        assertNull("No events", queue.take(1000));
    }

    @Test
    public void testPutTake() throws SinkException, SourceException {
        IQueue queue = createQueue();
        passTake(queue);
    }

    @Test
    public void testPutTake2Pass() throws SinkException, SourceException {
        IQueue queue = createQueue();
        passTake(queue);
        passTake(queue);
    }

    @Test
    public void testSinkClosedException() {
        IQueue queue = createQueue();

        Thread.currentThread().interrupt();
        try {
            queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                    new Event("2"),
                    new Event("3"),
                    new Event("4"),
                    new Event("5"),
                    new Event("6")}));
            fail("Must be SourceException");
        } catch (SinkException e) {
        }
    }

    @Test
    public void testSinkExceptionClogged() throws SinkException, SourceException {
        IQueue queue = createQueue();

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

        assertEquals("Sink size", 10, queue.size());
        assertEquals("WrongQueueEstimateSize", 10, queue.getEstimatedCount());
        try {
            queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                    new Event("11"), new Event("12"), new Event("13"), new Event("14")}));
            fail("Must be SinkException.Clogged");
        } catch (SinkException.Clogged e) {
        }

        assertEquals("Sink size", 10, queue.size());
        assertEquals("WrongQueueEstimateSize", 10, queue.getEstimatedCount());

        int i = 0;
        Collection<Event> events = queue.take(4);
        for (Event e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }

        assertEquals("EventCollection size", 4, events.size());
        assertEquals("Sink size", 6, queue.size());
        assertEquals("WrongQueueEstimateSize", 6, queue.getEstimatedCount());


        try {
            queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                    new Event("11"), new Event("12"), new Event("13"), new Event("14")}));
            fail("Must be SinkException.Clogged");
        } catch (SinkException.Clogged e) {
        }

        assertEquals("Sink size", 6, queue.size());
        assertEquals("WrongQueueEstimateSize", 6, queue.getEstimatedCount());


        queue.put(Arrays.asList(new IEvent[]{new Event("11"), new Event("12"), new Event("13"), new Event("14")}));

        assertEquals("Sink size", 10, queue.size());
        assertEquals("WrongQueueEstimateSize", 10, queue.getEstimatedCount());

        try {
            queue.put(Arrays.asList(new IEvent[]{new Event("11")}));
            fail("Must be SinkException.Clogged");
        } catch (SinkException.Clogged e) {
        }

        assertEquals("Sink size", 10, queue.size());
        assertEquals("WrongQueueEstimateSize", 10, queue.getEstimatedCount());
    }

    @Test
    public void testSourceException() throws SinkException {
        IQueue queue = createQueue();
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));

        Thread.currentThread().interrupt();

        try {
            queue.take(100);
            fail("Must be SourceException");
        } catch (SourceException e) {
        }
    }

    @Test
    public void testTakeZero() throws SourceException, SinkException {
        IQueue queue = createQueue();

        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));

        assertNull("Zero take ", queue.take(0));
        assertEquals("WrongQueueSize", 6, queue.size());
        assertEquals("WrongEstimateQueueSize", 6, queue.getEstimatedCount());
    }

    @Test
    public void testTryPutTake() throws SinkException, SourceException {
        IQueue queue = createQueue();
        tryPutTake(queue);
    }

    @Test
    public void testTryPutTake2Pass() throws SinkException, SourceException {
        IQueue queue = createQueue();
        tryPutTake(queue);
        tryPutTake(queue);
    }

    private void passTake(IQueue queue) throws SinkException, SourceException {
        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));
        assertEquals("WrongQueueSize", 6, queue.size());

        int i = 0;
        Collection<Event> events = queue.take(1);
        for (Event e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }
        assertEquals("WrongSize", 1, events.size());
        assertEquals("WrongQueueSize", 5, queue.size());
        assertEquals("WrongQueueEstimateSize", 5, queue.getEstimatedCount());

        events = queue.take(2);
        for (Event e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }
        assertEquals("WrongSize", 2, events.size());
        assertEquals("WrongQueueSize", 3, queue.size());
        assertEquals("WrongQueueEstimateSize", 3, queue.getEstimatedCount());

        events = queue.take(3);
        for (Event e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }
        assertEquals("WrongQueueSize", 0, queue.size());
        assertEquals("WrongQueueEstimateSize", 0, queue.getEstimatedCount());
        assertEquals("WrongSize", 3, events.size());

        events = queue.take(1000);
        assertNull("No events", events);
        events = queue.take(1000);
        assertNull("No events", events);

        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));
        assertEquals("WrongQueueSize", 6, queue.size());
        assertEquals("WrongQueueEstimateSize", 6, queue.getEstimatedCount());

        events = queue.take(1000);
        i = 0;
        for (Event e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }
    }

    private void tryPutTake(IQueue queue) throws SinkException, SourceException {
        assertEquals(null, queue.tryPut(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")})));
        assertEquals("WrongQueueSize", 6, queue.size());
        assertEquals("WrongQueueEstimateSize", 6, queue.getEstimatedCount());

        int i = 0;
        Collection<Event> events = queue.take(1);
        for (Event e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }
        assertEquals("WrongSize", 1, events.size());
        assertEquals("WrongQueueSize", 5, queue.size());
        assertEquals("WrongQueueEstimateSize", 5, queue.getEstimatedCount());

        events = queue.take(2);
        for (Event e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }
        assertEquals("WrongSize", 2, events.size());
        assertEquals("WrongQueueSize", 3, queue.size());
        assertEquals("WrongQueueEstimateSize", 3, queue.getEstimatedCount());

        events = queue.take(3);
        for (Event e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }
        assertEquals("WrongQueueSize", 0, queue.size());
        assertEquals("WrongQueueEstimateSize", 0, queue.getEstimatedCount());
        assertEquals("WrongSize", 3, events.size());

        events = queue.take(1000);
        assertNull("No events", events);
        events = queue.take(1000);
        assertNull("No events", events);

        queue.put(Arrays.asList(new IEvent[]{new Event("1"),
                new Event("2"),
                new Event("3"),
                new Event("4"),
                new Event("5"),
                new Event("6")}));
        assertEquals("WrongQueueSize", 6, queue.size());
        assertEquals("WrongQueueEstimateSize", 6, queue.getEstimatedCount());

        events = queue.take(1000);
        i = 0;
        for (Event e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }
    }
}