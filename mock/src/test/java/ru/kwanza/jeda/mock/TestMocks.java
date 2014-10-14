package ru.kwanza.jeda.mock;

import ru.kwanza.jeda.api.*;
import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Guzanov Alexander
 */
public class TestMocks extends TestCase {

    public static final class TestEvent extends AbstractEvent {
        private String contextId;

        public TestEvent(String contextId) {
            this.contextId = contextId;
        }

        public String getContextId() {
            return contextId;
        }
    }

    @Override
    public void setUp() throws Exception {
        MockJedaManager.getInstance().clearAll();
    }

    public void testMockFlowBus() throws SinkException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-context.xml", TestMocks.class);
        IJedaManager manager = ctx.getBean(IJedaManager.class);

        manager.getFlowBus("TestFlowBus_1").put(Arrays.asList(new IEvent[]{new TestEvent("1"), new TestEvent("2")}));
        manager.getFlowBus("TestFlowBus_2").put(Arrays.asList(new IEvent[]{new TestEvent("3"), new TestEvent("4")}));

        ArrayList<IEvent> events = MockFlowBus.getEvents("TestFlowBus_1");
        assertEquals("Wrong event", "1", ((TestEvent) events.get(0)).getContextId());
        assertEquals("Wrong event", "2", ((TestEvent) events.get(1)).getContextId());
        assertEquals("Wrong size", 2, events.size());

        events = MockFlowBus.getEvents("TestFlowBus_2");
        assertEquals("Wrong event", "3", ((TestEvent) events.get(0)).getContextId());
        assertEquals("Wrong event", "4", ((TestEvent) events.get(1)).getContextId());
        assertEquals("Wrong size", 2, events.size());

        MockJedaManager.getInstance().clearAll();
        events = MockFlowBus.getEvents("TestFlowBus_1");
        assertEquals("Wrong size", 0, events.size());

        events = MockFlowBus.getEvents("TestFlowBus_2");
        assertEquals("Wrong size", 0, events.size());

        assertEquals("Wrong bus name", "TestFlowBus_1", MockJedaManager.getInstance().getFlowBus("TestFlowBus_1").getName());

        MockJedaManager.getInstance().clearAll();
    }



    public void testCurrentStage() throws BusException, SinkException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-context.xml", TestMocks.class);
        IJedaManager manager = ctx.getBean(IJedaManager.class);

        manager.getFlowBus("TestFlowBus_1").put(Arrays.asList(new IEvent[]{new TestEvent("1"), new TestEvent("2")}));

        ArrayList<IEvent> events = MockFlowBus.getEvents("TestFlowBus_1");
        assertEquals("Wrong event", "1", ((TestEvent) events.get(0)).getContextId());
        assertEquals("Wrong event", "2", ((TestEvent) events.get(1)).getContextId());
        assertEquals("Wrong size", 2, events.size());


        MockJedaManager.getInstance().clearAll();
        events = MockFlowBus.getEvents("TestFlowBus_1");
        assertEquals("Wrong size", 0, events.size());

        MockJedaManager.getInstance().clearAll();
    }


    public void testBusException() throws BusException, SinkException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-context.xml", TestMocks.class);
        IJedaManager manager = ctx.getBean(IJedaManager.class);

        manager.getFlowBus("TestFlowBus_1").put(Arrays.asList(new IEvent[]{new TestEvent("1"), new TestEvent("2")}));
        MockJedaManager.getInstance().getFlowBus("TestFlowBus_1").setMaxSize(3);
        try {
            manager.getFlowBus("TestFlowBus_1").put(Arrays.asList(new IEvent[]{new TestEvent("1"), new TestEvent("2")}));
            fail("Mus be bus exception!");
        } catch (SinkException e) {
        }

        MockJedaManager.getInstance().clearAll();
    }

    public void testMockStage() {
        MockStage testStage = MockJedaManager.getInstance().getStage("TestStage");
        assertEquals("Wrong stage", "TestStage", testStage.getName());

        try {
            testStage.getThreadManager();
            fail("Must be exception!");
        } catch (UnsupportedOperationException e) {
        }

        try {
            testStage.getProcessor();
            fail("Must be exception!");
        } catch (UnsupportedOperationException e) {
        }

        try {
            testStage.getQueue();
            fail("Must be exception!");
        } catch (UnsupportedOperationException e) {
        }

        try {
            testStage.getAdmissionController();
            fail("Must be exception!");
        } catch (UnsupportedOperationException e) {
        }

        try {
            testStage.hasTransaction();
            fail("Must be exception!");
        } catch (UnsupportedOperationException e) {
        }

        assertEquals(MockSink.class, testStage.getSink().getClass());
    }


    private static class Event_1 extends AbstractEvent {

        public String getContextId() {
            return "1";
        }
    }

    private static class Event_2 extends AbstractEvent {

        public String getContextId() {
            return "1";
        }
    }

    public void testMockSink() throws SinkException {
        MockSink.setMaxSinkSize("TestStage", 10);
        assertEquals("Assert maxsize", 10, MockSink.getSink("TestStage").getMaxSize());

        ArrayList<IEvent> list = new ArrayList<IEvent>();
        for (int i = 0; i < 10; i++) {
            list.add(new Event_1());
        }

        MockJedaManager.getInstance().getStage("TestStage").getSink().put(list);

        assertEquals("Event count", 10, MockSink.getSink("TestStage").getEvents().size());
        assertEquals("Event count", 1, MockSink.getSink("TestStage").getEventsByClass().size());
        assertEquals("Event count", 10, MockSink.getSink("TestStage").getEventsByClass().get(Event_1.class).size());

        MockSink.getSink("TestStage").clear();
        MockSink.setMaxSinkSize("TestStage", 10);
        assertEquals("Event count", 0, MockSink.getSink("TestStage").getEvents().size());
        assertEquals("Event count", 0, MockSink.getSink("TestStage").getEventsByClass().size());


        list = new ArrayList<IEvent>();
        for (int i = 0; i < 20; i++) {
            list.add(new Event_1());
        }

        try {
            MockJedaManager.getInstance().getStage("TestStage").getSink().put(list);
            fail("Expected SinkException.Clogged");
        } catch (SinkException.Clogged e) {
        }

        assertEquals("Event count", 0, MockSink.getSink("TestStage").getEvents().size());
        assertEquals("Event count", 0, MockSink.getSink("TestStage").getEventsByClass().size());

        assertEquals("Clogged size", 10, MockJedaManager.getInstance().getStage("TestStage").getSink().tryPut(list).size());
        assertEquals("Event count", 10, MockSink.getSink("TestStage").getEvents().size());
        assertEquals("Event count", 1, MockSink.getSink("TestStage").getEventsByClass().size());
        assertEquals("Event count", 10, MockSink.getSink("TestStage").getEventsByClass().get(Event_1.class).size());


        MockSink.getSink("TestStage").clear();
        MockSink.setMaxSinkSize("TestStage", 10);
        assertEquals("Event count", 0, MockSink.getSink("TestStage").getEvents().size());
        assertEquals("Event count", 0, MockSink.getSink("TestStage").getEventsByClass().size());


        list = new ArrayList<IEvent>();
        for (int i = 0; i < 5; i++) {
            list.add(new Event_1());
        }

        for (int i = 0; i < 5; i++) {
            list.add(new Event_2());
        }

        MockJedaManager.getInstance().getStage("TestStage").getSink().put(list);
        assertEquals("Event count", 10, MockSink.getSink("TestStage").getEvents().size());
        assertEquals("Event count", 2, MockSink.getSink("TestStage").getEventsByClass().size());
        assertEquals("Event count", 5, MockSink.getSink("TestStage").getEventsByClass().get(Event_1.class).size());
        assertEquals("Event count", 5, MockSink.getSink("TestStage").getEventsByClass().get(Event_2.class).size());

        MockSink.getSink("TestStage").clear();
        MockSink.setMaxSinkSize("TestStage", 10);
        assertEquals("Event count", 0, MockSink.getSink("TestStage").getEvents().size());
        assertEquals("Event count", 0, MockSink.getSink("TestStage").getEventsByClass().size());

        list = new ArrayList<IEvent>();
        for (int i = 0; i < 5; i++) {
            list.add(new Event_1());
        }

        assertNull("Clogged size", MockJedaManager.getInstance().getStage("TestStage").getSink().tryPut(list));
        assertEquals("Event count", 5, MockSink.getSink("TestStage").getEvents().size());
        assertEquals("Event count", 1, MockSink.getSink("TestStage").getEventsByClass().size());
        assertEquals("Event count", 5, MockSink.getSink("TestStage").getEventsByClass().get(Event_1.class).size());

    }
}
