package ru.kwanza.jeda.api;

import junit.framework.TestCase;

import java.nio.channels.IllegalSelectorException;

/**
 * @author Guzanov Alexander
 */
public class TestTimerItem extends TestCase {

    private static final class Event extends AbstractEvent {
        private String contextId;

        private Event(String contextId) {
            this.contextId = contextId;
        }

        public String getContextId() {
            return contextId;
        }
    }

    public void testTimerItem() {
        TimerItem<Event> item1 = new TimerItem(new Event("Test1"), 1000);
        TimerItem<Event>  item2 = new TimerItem(new Event("Test2"), 2000);
        TimerItem<Event>  item3 = new TimerItem(new Event("Test3"), 1000);
        TimerItem<Event>  item4 = new TimerItem(new Event("Test4"), 2000);


        assertEquals(item1.getMillis(), 1000);
        assertEquals(item2.getMillis(), 2000);
        assertEquals(item3.getMillis(), 1000);
        assertEquals(item4.getMillis(), 2000);


        assertEquals(item1.getEvent().getContextId(), "Test1");
        assertEquals(item2.getEvent().getContextId(), "Test2");
        assertEquals(item3.getEvent().getContextId(), "Test3");
        assertEquals(item4.getEvent().getContextId(), "Test4");


        item1.setTimerHandle("handle");

        assertEquals(item1.getTimerHandle(), "handle");

        try {
            item2.getTimerHandle();
            fail("Expected " + IllegalSelectorException.class);
        } catch (IllegalStateException e) {

        }
    }

}
