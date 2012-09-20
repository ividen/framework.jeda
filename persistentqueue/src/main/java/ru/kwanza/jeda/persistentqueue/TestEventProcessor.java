package ru.kwanza.jeda.persistentqueue;

import ru.kwanza.jeda.api.IEventProcessor;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Guzanov Alexander
 */
public class TestEventProcessor implements IEventProcessor<TestEvent> {
    public static AtomicLong counter = new AtomicLong(0);
    public static AtomicLong expectedSize = new AtomicLong(-1);

    private AtomicLong start = new AtomicLong(0l);
    private AtomicLong finish = new AtomicLong(0l);

    public void process(Collection<TestEvent> events) {
        if (start.get() == 0) {
            start.set(System.currentTimeMillis());
        }

        long l = counter.addAndGet(events.size());

        if (l == expectedSize.get()) {
            long ts = System.currentTimeMillis() - start.get();
            System.out.println("Finish Process avg=" + l * 1000 / (ts));
        }

    }
}
