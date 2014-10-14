package ru.kwanza.jeda.mock;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.ISink;
import ru.kwanza.jeda.api.SinkException;

import java.util.*;

/**
 * @author Guzanov Alexander
 */
public class MockSink implements ISink<IEvent> {
    private Long maxSize = Long.MAX_VALUE;
    private long size = 0;
    private ArrayList<IEvent> events = new ArrayList<IEvent>();
    private Map<Class, List<IEvent>> eventsByClass = new HashMap<Class, List<IEvent>>();

    public final class MockSinkSync implements MockTxSync {
        private Collection<IEvent> elements;

        public MockSinkSync(Collection<IEvent> elements) {
            this.elements = elements;
            size += elements.size();
        }

        public synchronized void commit() {
            for (IEvent e : elements) {
                add(e);
            }

        }

        public synchronized void rollback() {
            size -= elements.size();
        }
    }

    public synchronized void put(Collection<IEvent> events) throws SinkException {
        if (size + events.size() > maxSize) {
            throw new SinkException.Clogged("Max size=" + maxSize + "!");
        }

            for (IEvent e : events) {
                add(e);
            }
            size += events.size();
    }

    private void add(IEvent e) {
        events.add(e);
        List<IEvent> list = eventsByClass.get(e.getClass());
        if (list == null) {
            list = new ArrayList<IEvent>();
            eventsByClass.put(e.getClass(), list);
        }
        list.add(e);
    }

    public synchronized Collection<IEvent> tryPut(Collection<IEvent> events) throws SinkException {
        ArrayList<IEvent> result = new ArrayList<IEvent>();
        for (IEvent e : events) {
            if (this.events.size() < maxSize) {
                add(e);
            } else {
                result.add(e);
            }
        }

        if (result.isEmpty()) {
            return null;
        }

        return result;
    }

    public synchronized void clear() {
        events.clear();
        eventsByClass.clear();
        maxSize = Long.MAX_VALUE;
        size = 0;
    }

    public ArrayList<IEvent> getEvents() {
        return events;
    }

    public Map<Class, List<IEvent>> getEventsByClass() {
        return eventsByClass;
    }

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public static void setMaxSinkSize(String stageName, long value) {
        MockJedaManager.getInstance().getStageInternal(stageName).getSink().setMaxSize(value);
    }

    public static MockSink getSink(String stageName) {
        return MockJedaManager.getInstance().getStage(stageName).getSink();
    }
}
