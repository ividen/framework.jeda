package ru.kwanza.jeda.core.queue;

import ru.kwanza.jeda.api.IEvent;
import junit.framework.TestCase;

import java.util.Collection;
import java.util.Iterator;


/**
 * @author Guzanov Alexander
 */
public class TestImmutableEventCollection extends TestCase {
    public void testAddWithUnsupportedOperationException() {
        Collection<IEvent> events = createCollection();

        try {
            events.add(new Event("7"));
        } catch (Exception e) {
            assertEquals("Wrong Exception class", UnsupportedOperationException.class.getName(), e.getClass().getName());
            return;
        }

        fail("Expected exception");
    }

    public void testIterate() {
        Collection<Event> events = createCollection();

        int i = 0;
        for (Event e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }

        i = 0;
        for (Event e : events) {
            i++;
            assertEquals(String.valueOf(i), e.getContextId());
        }

        assertEquals("Wrong event size", 6, events.size());
    }

    public void testRemoveWithUnsupportedOperationException() {
        Collection<IEvent> events = createCollection();
        Iterator<IEvent> iterator = events.iterator();
        try {
            iterator.remove();
        } catch (Exception e) {
            assertEquals("Wrong Exception class", UnsupportedOperationException.class.getName(), e.getClass().getName());
            return;
        }

        fail("Expected exception");
    }

    public void testWrongIterate() {
        Collection<Event> events = createCollection();
        assertEquals("Wrong event size", 6, events.size());
        Iterator<Event> iterator = events.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            i++;
            assertEquals(String.valueOf(i), iterator.next().getContextId());
        }

        try {
            iterator.next();
        } catch (Exception e) {
            assertEquals("Wrong Exception class", IllegalStateException.class.getName(), e.getClass().getName());
            return;
        }

        fail("Expected exception");
    }

    private ImmutableEventCollection createCollection() {
        Node n = new Node(new Event("1"));
        Node n2 = new Node(new Event("2"));
        Node n3 = new Node(new Event("3"));
        Node n4 = new Node(new Event("4"));
        Node n5 = new Node(new Event("5"));
        Node n6 = new Node(new Event("6"));

        n.next = n2;
        n2.next = n3;
        n3.next = n4;
        n4.next = n5;
        n5.next = n6;
        n6.next = null;


        return new ImmutableEventCollection(n, 6);
    }
}
