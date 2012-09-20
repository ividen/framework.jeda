package ru.kwanza.jeda.api;

import junit.framework.TestCase;

/**
 * @author Guzanov Alexander
 */
public class TestPriority extends TestCase {

    public void testPriority() {
        assertEquals(IPriorityEvent.Priority.CRITICAL.getCode(), 5);
        assertEquals(IPriorityEvent.Priority.HIGHEST.getCode(), 4);
        assertEquals(IPriorityEvent.Priority.HIGH.getCode(), 3);
        assertEquals(IPriorityEvent.Priority.NORMAL.getCode(), 2);
        assertEquals(IPriorityEvent.Priority.LOW.getCode(), 1);

        assertEquals(IPriorityEvent.Priority.CRITICAL, IPriorityEvent.Priority.findByCode(5));
        assertEquals(IPriorityEvent.Priority.HIGHEST, IPriorityEvent.Priority.findByCode(4));
        assertEquals(IPriorityEvent.Priority.HIGH, IPriorityEvent.Priority.findByCode(3));
        assertEquals(IPriorityEvent.Priority.NORMAL, IPriorityEvent.Priority.findByCode(2));
        assertEquals(IPriorityEvent.Priority.LOW, IPriorityEvent.Priority.findByCode(1));
        assertNull(IPriorityEvent.Priority.findByCode(0));

    }
}
