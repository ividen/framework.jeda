package ru.kwanza.jeda.core.queue;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.api.internal.IQueueObserver;
import ru.kwanza.jeda.api.internal.SourceException;
import junit.framework.TestCase;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class TestAbstractObservableQueue extends TestCase {
    public static class TestQueue<E extends IEvent> extends AbstractObservableMemoryQueue<E> {
        public int getEstimatedCount() {
            return 0;
        }

        public boolean isReady() {
            return size() > 0;
        }

        public void put(Collection<E> events) throws SinkException {
        }

        public Collection<E> tryPut(Collection<E> events) throws SinkException {
            return null;
        }

        public Collection<E> take(int count) throws SourceException {
            return null;
        }

        public long size() {
            return 0;
        }

        @Override
        public void notify(long size, long delta) {
            super.notify(size, delta);
        }
    }

    public static class TestObserver implements IQueueObserver {
        private long queueSize;
        private long delta;


        public void notifyChange(long queueSize, long delta) {
            this.queueSize = queueSize;
            this.delta = delta;
        }
    }

    public void testNotify() {
        TestObserver observer = new TestObserver();
        TestQueue testQueue = new TestQueue();
        testQueue.setObserver(observer);

        testQueue.notify(10, 1);

        assertEquals("QueueSize", 10, observer.queueSize);
        assertEquals("delta", 1, observer.delta);


        testQueue.notify(5, 0);

        assertEquals("QueueSize", 10, observer.queueSize);
        assertEquals("delta", 1, observer.delta);
    }
}
