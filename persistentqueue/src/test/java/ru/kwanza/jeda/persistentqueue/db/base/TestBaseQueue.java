package ru.kwanza.jeda.persistentqueue.db.base;

import org.junit.Test;
import ru.kwanza.jeda.persistentqueue.DefaultPersistableEvent;
import ru.kwanza.jeda.persistentqueue.DefaultPriorityPersistableEvent;
import ru.kwanza.toolbox.SerializationHelper;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static ru.kwanza.jeda.api.IPriorityEvent.Priority.CRITICAL;

/**
 * @author Alexander Guzanov
 */
public class TestBaseQueue {

    @Test
    public void testBaseEventQueue() throws Exception {
        BaseEventQueue<DefaultPersistableEvent> o = new BaseEventQueue<DefaultPersistableEvent>(10l, 1,
                SerializationHelper.objectToBytes(new DefaultPersistableEvent(1l)));

        assertEquals(o.getId(), Long.valueOf(10l));
        assertEquals(o.getNodeId(), 1);
        assertEquals(o.getEvent().getPersistId(), Long.valueOf(10l));

        o.setNodeId(2);
        assertEquals(o.getNodeId(), 2);

         o = new BaseEventQueue<DefaultPersistableEvent>(10l, 1,
                new byte[0]);

        try {
            o.getEvent();
            fail("Expected RuntimeException!");
        }catch (RuntimeException e){

        }
    }

    @Test
    public void testBaseEventQueueWithName() throws Exception {
        BaseNamedEventQueue<DefaultPersistableEvent> o = new BaseNamedEventQueue<DefaultPersistableEvent>(10l, 1,
                SerializationHelper.objectToBytes(new DefaultPersistableEvent(1l)), "test_queue");

        assertEquals(o.getId(), Long.valueOf(10l));
        assertEquals(o.getNodeId(), 1);
        assertEquals(o.getEvent().getPersistId(), Long.valueOf(10l));
        assertEquals(o.getQueueName(), "test_queue");
    }

    @Test
    public void testBasePriorityEventQueue() throws Exception {
        BasePriorityEventQueue<DefaultPriorityPersistableEvent> o
                = new BasePriorityEventQueue<DefaultPriorityPersistableEvent>(10l, 1,
                SerializationHelper.objectToBytes(
                        new DefaultPriorityPersistableEvent(1l, CRITICAL)),
                CRITICAL.getCode());

        assertEquals(o.getId(), Long.valueOf(10l));
        assertEquals(o.getNodeId(), 1);
        assertEquals(o.getEvent().getPersistId(), Long.valueOf(10l));
        assertEquals(o.getEvent().getPriority(), CRITICAL);
        assertEquals(o.getPriority().intValue(), CRITICAL.getCode());
    }

    @Test
    public void testBasePriorityEventQueueWithName() throws Exception {
        BasePriorityEventQueueWithQueueName<DefaultPriorityPersistableEvent> o
                = new BasePriorityEventQueueWithQueueName<DefaultPriorityPersistableEvent>(10l, 1,
                SerializationHelper.objectToBytes(
                        new DefaultPriorityPersistableEvent(1l, CRITICAL)),
                CRITICAL.getCode(), "test_queue");

        assertEquals(o.getId(), Long.valueOf(10l));
        assertEquals(o.getNodeId(), 1);
        assertEquals(o.getEvent().getPersistId(), Long.valueOf(10l));
        assertEquals(o.getEvent().getPriority(), CRITICAL);
        assertEquals(o.getPriority().intValue(), CRITICAL.getCode());
        assertEquals(o.getQueueName(), "test_queue");
    }
}
