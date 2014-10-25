package ru.kwanza.jeda.persistentqueue.old;

import junit.framework.TestCase;
import oracle.net.jdbc.nl.UninitializedObjectException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Guzanov Alexander
 */
public class TestEventWithKey extends TestCase {
    public void testEventWithKey() {
        Event test = new Event("test");

        try {
            new EventWithKey(null, test);
            fail("Expected " + NullPointerException.class);
        } catch (NullPointerException e) {
        }

        EventWithKey eventWithKey = new EventWithKey(test);

        assertEquals(eventWithKey.getDelegate(), test);
        assertNull(eventWithKey.getKey());

        eventWithKey.setKey(1l);
        assertEquals(eventWithKey.getKey(), 1l);


        eventWithKey = new EventWithKey(2l, test);

        assertEquals(eventWithKey.getDelegate(), test);
        assertEquals(eventWithKey.getKey(), 2l);
        eventWithKey.setKey(3l);
        assertEquals(eventWithKey.getKey(), 3l);
    }


    public void testCollection() {

        ArrayList<Event> events = new ArrayList<Event>(10);
        ArrayList<EventWithKey> eventsWithKey = new ArrayList<EventWithKey>(10);
        for (int i = 0; i < 10; i++) {
            events.add(new Event("test" + i));
        }

        for (Event e : events) {
            eventsWithKey.add(new EventWithKey(e));
        }

        Collection<Event> extract = EventWithKey.<Event>extract(eventsWithKey);

        assertEquals(extract.size(), 10);
        for (int i = 0; i < 10; i++) {
            String searchString = "test" + i;
            boolean find = false;
            for (Event e : extract) {
                if (e.getContextId().equals(searchString)) {
                    find = true;
                    break;
                }
            }
            assertTrue("Finded " + searchString, find);
        }

        Iterator<Event> iterator = extract.iterator();
        iterator.next();
        try {
            iterator.remove();
            fail("Expected " + UninitializedObjectException.class);
        } catch (UnsupportedOperationException e) {

        }
    }

    public void testExtractNull() {
        assertNull(EventWithKey.extract(null));
    }
}
