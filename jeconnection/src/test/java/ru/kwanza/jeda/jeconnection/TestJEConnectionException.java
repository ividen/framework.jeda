package ru.kwanza.jeda.jeconnection;

import junit.framework.TestCase;

/**
 * @author Guzanov Alexander
 */
public class TestJEConnectionException extends TestCase {
    public void testSourceException() {
        JEConnectionException e1 = new JEConnectionException();
        assertNull(e1.getMessage());
        assertNull(e1.getCause());
        JEConnectionException e2 = new JEConnectionException("Test");
        assertEquals(e2.getMessage(), "Test");
        assertNull(e2.getCause());
        JEConnectionException e3 = new JEConnectionException("Test", new RuntimeException());
        assertEquals(e3.getMessage(), "Test");
        assertEquals(e3.getCause().getClass(), RuntimeException.class);
        JEConnectionException e4 = new JEConnectionException(new RuntimeException());
        assertEquals(e4.getMessage(), "java.lang.RuntimeException");
        assertEquals(e4.getCause().getClass(), RuntimeException.class);
    }
}
