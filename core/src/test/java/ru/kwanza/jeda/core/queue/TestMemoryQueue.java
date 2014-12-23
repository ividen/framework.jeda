package ru.kwanza.jeda.core.queue;


import org.junit.Test;
import ru.kwanza.jeda.api.internal.IQueue;

import static junit.framework.Assert.assertEquals;

public class TestMemoryQueue extends TestEventQueue {
    @Test
    public void testMaxSize() {
        MemoryQueue memoryQueue = new MemoryQueue();
        assertEquals("MaxSize wrong", Integer.MAX_VALUE, memoryQueue.getMaxSize());
    }

    @Override
    protected IQueue createQueue() {
        return new MemoryQueue(10);
    }
}
