package ru.kwanza.jeda.core.queue;


import ru.kwanza.jeda.api.internal.IQueue;
import junit.framework.TestSuite;

public class TestMemoryQueue extends TestEventQueue {
    public static TestSuite suite() {
        return new TestSuite(TestMemoryQueue.class);
    }

    public void testMaxSize() {
        MemoryQueue memoryQueue = new MemoryQueue();
        assertEquals("MaxSize wrong", Long.MAX_VALUE, memoryQueue.getMaxSize());
    }

    @Override
    protected IQueue createQueue() {
        return new MemoryQueue(10);
    }
}
