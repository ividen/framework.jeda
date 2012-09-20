package ru.kwanza.jeda.core.pendingstore.env;

import java.util.*;

/**
 * @author Dmitry Zagorovsky
 */
public class FlowBusEventStore {

    private static FlowBusEventStore instance = new FlowBusEventStore();

    private Map<String, List<TestEvent>> eventsBySink = new HashMap<String, List<TestEvent>>();

    private FlowBusEventStore() {
    }

    public static List<TestEvent> getEvents(String sinkName) {
        return instance.eventsBySink.get(sinkName);
    }

    public static void put(String sinkName, Collection<TestEvent> events) {
        instance.put0(sinkName, events);
    }

    public static void clear() {
        instance.eventsBySink.clear();
    }

    private void put0(String sinkName, Collection<TestEvent> events) {
        List<TestEvent> eventList = eventsBySink.get(sinkName);
        if (eventList == null) {
            eventList = new ArrayList<TestEvent>();
            eventsBySink.put(sinkName, eventList);
        }
        eventList.addAll(events);
    }

}
