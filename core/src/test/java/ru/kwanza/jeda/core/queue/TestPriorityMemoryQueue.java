package ru.kwanza.jeda.core.queue;

import ru.kwanza.jeda.api.internal.IQueue;
import junit.framework.TestSuite;

/**
 * @author Guzanov Alexander
 */
public class TestPriorityMemoryQueue extends TestPriorityEventQueue {
    public static TestSuite suite() {
        return new TestSuite(TestPriorityMemoryQueue.class);
    }

    public void testMaxSize() {
        PriorityMemoryQueue memoryQueue = new PriorityMemoryQueue();
        assertEquals("MaxSize wrong", Long.MAX_VALUE, memoryQueue.getMaxSize());
    }

    @Override
    protected IQueue createQueue() {
        return new PriorityMemoryQueue(10);
    }
}