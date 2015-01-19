package ru.kwanza.jeda.core.queue;

import org.junit.Test;
import ru.kwanza.jeda.api.internal.IQueue;

import static junit.framework.Assert.assertEquals;

/**
 * @author Guzanov Alexander
 */
public class TestPriorityMemoryQueue extends TestPriorityEventQueue {
    @Test
    public void testMaxSize() {
        PriorityMemoryQueue memoryQueue = new PriorityMemoryQueue();
        assertEquals("MaxSize wrong", Integer.MAX_VALUE, memoryQueue.getMaxSize());
    }

    @Override
    protected IQueue createQueue() {
        return new PriorityMemoryQueue(10);
    }
}