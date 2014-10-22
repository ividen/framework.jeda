package ru.kwanza.jeda.mock;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.IFlowBus;
import ru.kwanza.jeda.api.SinkException;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class MockFlowBus implements IFlowBus<IEvent> {
    private String name;
    private ArrayList<IEvent> events = new ArrayList<IEvent>();
    private int maxSize = Integer.MAX_VALUE;
    private long size;

    public final class MockFlowBusSync implements MockTxSync {
        private Collection<IEvent> elements;

        public MockFlowBusSync(Collection<IEvent> elements) {
            this.elements = elements;
            size += elements.size();
        }

        public synchronized void commit() {
            events.addAll(elements);
        }

        public synchronized void rollback() {
            size -= elements.size();
        }
    }


    public MockFlowBus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public synchronized void put(Collection<IEvent> events) throws SinkException {
        if (size + events.size() > maxSize) {
            throw new SinkException("Max size=" + maxSize + "!");
        }


            this.events.addAll(events);
            size += events.size();
    }

    public Collection<IEvent> tryPut(Collection<IEvent> events) throws SinkException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ArrayList<IEvent> getEvents() {
        return events;
    }


    public static ArrayList<IEvent> getEvents(String name) {
        return MockJedaManager.getInstance().getFlowBus(name).getEvents();
    }

    public static ArrayList<IEvent> getCurrentFlowEvents() {
        return MockJedaManager.getInstance().getFlowBus(MockJedaManager.MOCK_CURRENT_STAGE).getEvents();
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
}
