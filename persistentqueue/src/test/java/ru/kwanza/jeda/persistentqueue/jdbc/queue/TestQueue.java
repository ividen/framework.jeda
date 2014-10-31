package ru.kwanza.jeda.persistentqueue.jdbc.queue;

import org.junit.Test;
import ru.kwanza.jeda.persistentqueue.DefaultPersistableEvent;
import ru.kwanza.jeda.persistentqueue.DefaultPriorityPersistableEvent;
import ru.kwanza.jeda.persistentqueue.jdbc.base.BasePriorityEventQueueWithQueueName;

import java.io.PrintStream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static ru.kwanza.jeda.api.IPriorityEvent.Priority.CRITICAL;

/**
 * @author Alexander Guzanov
 */
public class TestQueue {

    public static class NonPersistantEvent extends DefaultPersistableEvent {
        private PrintStream ps;

        public NonPersistantEvent(Long persistId, PrintStream ps) {
            super(persistId);
            this.ps = ps;
        }
    }

    public static class NonPersistantPriorityEvent extends DefaultPriorityPersistableEvent {
        private PrintStream ps;

        public NonPersistantPriorityEvent(Long persistId, Priority p, PrintStream ps) {
            super(persistId, p);
            this.ps = ps;
        }
    }

    @Test
    public void testEventQueue() throws Exception {
        EventQueue<DefaultPersistableEvent> o =
                new EventQueue.Builder().build(new DefaultPersistableEvent(10l), 1);

        assertEquals(o.getId(), Long.valueOf(10l));
        assertEquals(o.getNodeId(), 1);
        assertEquals(o.getEvent().getPersistId(), Long.valueOf(10l));

        o.setNodeId(2);
        assertEquals(o.getNodeId(), 2);
    }


    @Test
    public void testEventQueue_fail() throws Exception {
        try {
            EventQueue<DefaultPersistableEvent> o =
                    new EventQueue.Builder().build(new NonPersistantEvent(10l, System.out), 1);
            fail("Expected RuntimeException!");
        } catch (RuntimeException e) {

        }
    }

    @Test
    public void testEventQueueWithName() throws Exception {
        NamedEventQueue<DefaultPersistableEvent> o =
                new NamedEventQueue.Builder("test_queue")
                        .build(new DefaultPersistableEvent(10l), 1);

        assertEquals(o.getId(), Long.valueOf(10l));
        assertEquals(o.getNodeId(), 1);
        assertEquals(o.getEvent().getPersistId(), Long.valueOf(10l));
        assertEquals(o.getQueueName(), "test_queue");
    }

    @Test
    public void testEventQueueWithName_fail() throws Exception {
        try {
            NamedEventQueue<DefaultPersistableEvent> o =
                    new NamedEventQueue.Builder("test_queue")
                            .build(new NonPersistantEvent(10l, System.out), 1);
            fail("Expected RuntimeException!");
        } catch (RuntimeException e) {
        }
    }

    @Test
    public void testBasePriorityEventQueue() throws Exception {
        PriorityEventQueue<DefaultPriorityPersistableEvent> o
                = new PriorityEventQueue.Builder().build(
                new DefaultPriorityPersistableEvent(10l, CRITICAL), 1);

        assertEquals(o.getId(), Long.valueOf(10l));
        assertEquals(o.getNodeId(), 1);
        assertEquals(o.getEvent().getPersistId(), Long.valueOf(10l));
        assertEquals(o.getEvent().getPriority(), CRITICAL);
        assertEquals(o.getPriority().intValue(), CRITICAL.getCode());
    }

    @Test
    public void testBasePriorityEventQueue_fail() throws Exception {
        try {
            PriorityEventQueue<DefaultPriorityPersistableEvent> o
                    = new PriorityEventQueue.Builder().build(
                    new NonPersistantPriorityEvent(10l, CRITICAL, System.out), 1);

        } catch (RuntimeException e) {
        }
    }

    @Test
    public void testBasePriorityEventQueueWithName() throws Exception {
        BasePriorityEventQueueWithQueueName<DefaultPriorityPersistableEvent> o
                = new NamedPriorityEventQueue.Builder("test_queue")
                .build(new DefaultPriorityPersistableEvent(10l, CRITICAL), 1);

        assertEquals(o.getId(), Long.valueOf(10l));
        assertEquals(o.getNodeId(), 1);
        assertEquals(o.getEvent().getPersistId(), Long.valueOf(10l));
        assertEquals(o.getEvent().getPriority(), CRITICAL);
        assertEquals(o.getPriority().intValue(), CRITICAL.getCode());
        assertEquals(o.getQueueName(), "test_queue");
    }

    @Test
    public void testBasePriorityEventQueueWithName_fail() throws Exception {
        try {
            BasePriorityEventQueueWithQueueName<DefaultPriorityPersistableEvent> o
                    = new NamedPriorityEventQueue.Builder("test_queue")
                    .build(new NonPersistantPriorityEvent(10l, CRITICAL, System.out), 1);
        } catch (RuntimeException e) {
        }
    }
}
