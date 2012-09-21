package ru.kwanza.jeda.core.pendingstore.env;

import ru.kwanza.jeda.api.IFlowBus;
import ru.kwanza.jeda.api.SinkException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static ru.kwanza.jeda.core.pendingstore.env.FlowBusBehaviour.SinkExceptionType;

public class TestFlowBus1 implements IFlowBus<TestEvent> {

    public void put(Collection<TestEvent> events) throws SinkException {
        processTestException();
        FlowBusEventStore.put(getClass().getSimpleName(), events);
    }

    public Collection<TestEvent> tryPut(Collection<TestEvent> events) throws SinkException {
        processTestException();

        LinkedList<TestEvent> inEventList = new LinkedList<TestEvent>(events);
        List<TestEvent> remainEvents = new ArrayList<TestEvent>();

        int remainTryPutCount = FlowBusBehaviour.getRemainTryPutCount();
        if (remainTryPutCount > 0) {
            for (int i = 0; i < remainTryPutCount; i++) {
                remainEvents.add(inEventList.removeLast());
            }
        }

        FlowBusEventStore.put(getClass().getSimpleName(), inEventList);

        return remainEvents;
    }

    private void processTestException() throws SinkException {
        SinkExceptionType exceptionType = FlowBusBehaviour.getSinkExceptionType();
        if (exceptionType != null) {
            switch (exceptionType) {
                case CLOGGED:
                    throw new SinkException.Clogged("Test Sink Clogged.");
                case CLOSED:
                    throw new SinkException.Closed("Test Sink Closed.");
                case OTHER:
                    throw new SinkException("Test Sink Exception.");
            }

        }
    }

}

