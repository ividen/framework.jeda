package ru.kwanza.jeda.api;

import ru.kwanza.jeda.api.internal.SourceException;
import junit.framework.TestCase;

import java.util.ArrayList;

/**
 * @author Guzanov Alexander
 */
public class TestException extends TestCase {

    public void testSinkException() {
        SinkException e1 = new SinkException();
        assertNull(e1.getMessage());
        assertNull(e1.getCause());
        SinkException e2 = new SinkException("Test");
        assertEquals(e2.getMessage(), "Test");
        assertNull(e2.getCause());
        SinkException e3 = new SinkException("Test", new RuntimeException());
        assertEquals(e3.getMessage(), "Test");
        assertEquals(e3.getCause().getClass(), RuntimeException.class);
        SinkException e4 = new SinkException(new RuntimeException());
        assertEquals(e4.getMessage(), "java.lang.RuntimeException");
        assertEquals(e4.getCause().getClass(), RuntimeException.class);
    }

    public void testSinkException_Clogged() {
        SinkException e2 = new SinkException.Clogged("Test");
        assertEquals(e2.getMessage(), "Test");
        assertNull(e2.getCause());
    }

    public void testSinkException_Closed() {
        SinkException e2 = new SinkException.Closed("Test");
        assertEquals(e2.getMessage(), "Test");
        assertNull(e2.getCause());
        SinkException e3 = new SinkException.Closed("Test", new RuntimeException());
        assertEquals(e3.getMessage(), "Test");
        assertEquals(e3.getCause().getClass(), RuntimeException.class);
        SinkException e4 = new SinkException.Closed(new RuntimeException());
        assertEquals(e4.getMessage(), "java.lang.RuntimeException");
        assertEquals(e4.getCause().getClass(), RuntimeException.class);
    }


    public void testBusException() {
        BusException e1 = new BusException();
        assertNull(e1.getMessage());
        assertNull(e1.getCause());
        BusException e2 = new BusException("Test");
        assertEquals(e2.getMessage(), "Test");
        assertNull(e2.getCause());
        BusException e3 = new BusException("Test", new RuntimeException());
        assertEquals(e3.getMessage(), "Test");
        assertEquals(e3.getCause().getClass(), RuntimeException.class);
        BusException e4 = new BusException(new RuntimeException());
        assertEquals(e4.getMessage(), "java.lang.RuntimeException");
        assertEquals(e4.getCause().getClass(), RuntimeException.class);
    }

    public void testSourceException() {
        SourceException e1 = new SourceException();
        assertNull(e1.getMessage());
        assertNull(e1.getCause());
        SourceException e2 = new SourceException("Test");
        assertEquals(e2.getMessage(), "Test");
        assertNull(e2.getCause());
        SourceException e3 = new SourceException("Test", new RuntimeException());
        assertEquals(e3.getMessage(), "Test");
        assertEquals(e3.getCause().getClass(), RuntimeException.class);
        SourceException e4 = new SourceException(new RuntimeException());
        assertEquals(e4.getMessage(), "java.lang.RuntimeException");
        assertEquals(e4.getCause().getClass(), RuntimeException.class);
    }

    public void testContextStoreException() {
        ArrayList optimisticItems = new ArrayList();
        ArrayList otherFailedItems = new ArrayList();
        ContextStoreException e1 = new ContextStoreException(optimisticItems, otherFailedItems);
        assertNull(e1.getMessage());
        assertNull(e1.getCause());
        assertEquals(optimisticItems, e1.<Object>getOptimisticItems());
        assertEquals(otherFailedItems, e1.<Object>getOtherFailedItems());
    }
}

