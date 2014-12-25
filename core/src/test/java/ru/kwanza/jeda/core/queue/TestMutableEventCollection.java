package ru.kwanza.jeda.core.queue;


import org.junit.Test;
import ru.kwanza.jeda.api.IEvent;

import java.util.Collection;
import java.util.Iterator;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

/**
 * @author: Guzanov Alexander
 */
public class TestMutableEventCollection {
    @Test
    public void testRemove() {
        Collection<Event> events = createCollection();

        int i = 0;
        for (Event e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }

        assertEquals("Wrong size", 6, events.size());

        Iterator<Event> iterator = events.iterator();
        iterator.next();
        iterator.remove();

        i = 1;
        for (Event e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }
        assertEquals("Wrong size", 5, events.size());
        assertEquals("Last element in list", 6, i);

        IEvent prev = null;
        iterator = events.iterator();
        for (int j = 2; j <= 6; j++) {
            iterator.next().getContextId();
        }

        iterator.remove();

        i = 1;
        for (Event e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }
        assertEquals("Last element in list", 5, i);
        assertEquals("Wrong size", 4, events.size());

        iterator = events.iterator();
        iterator.next();
        iterator.next();
        iterator.remove();
        iterator = events.iterator();

        assertEquals("Wrong size", 3, events.size());
        assertEquals("Wrong Element in list", "2", iterator.next().getContextId());
        assertEquals("Wrong Element in list", "4", iterator.next().getContextId());
        assertEquals("Wrong Element in list", "5", iterator.next().getContextId());
        assertEquals("Wrong ImmutableEventCollection size", 3, events.size());
        assertEquals("Must be only 3 elements", false, iterator.hasNext());


        iterator = events.iterator();
        iterator.next();
        iterator.remove();
        assertEquals("Wrong Element in list", "4", iterator.next().getContextId());
        assertEquals("Wrong Element in list", "5", iterator.next().getContextId());
        assertEquals("Must be only 3 elements", false, iterator.hasNext());
        assertEquals("Wrong ImmutableEventCollection size", 2, events.size());
    }

    @Test
    public void testRemoveWithIllegalStateException_1() {
        Collection<Event> events = createCollection();
        Iterator<Event> iterator = events.iterator();
        try {
            iterator.remove();
        } catch (Exception e) {
            assertEquals("Wrong Exception class", IllegalStateException.class.getName(), e.getClass().getName());
            return;
        }

        fail("Expected exception");
    }

    @Test
    public void testRemoveWithIllegalStateException_2() {
        Collection<Event> events = createCollection();
        Iterator<Event> iterator = events.iterator();
        iterator.next();
        iterator.remove();
        try {
            iterator.remove();
        } catch (Exception e) {
            assertEquals("Wrong Exception class", IllegalStateException.class.getName(), e.getClass().getName());
            return;
        }

        fail("Expected exception");
    }

    private MutableEventCollection createCollection() {
        MutableEventCollection result = new MutableEventCollection();
        result.add(new Event("1"));
        result.add(new Event("2"));
        result.add(new Event("3"));
        result.add(new Event("4"));
        result.add(new Event("5"));
        result.add(new Event("6"));
        return result;
    }
}
