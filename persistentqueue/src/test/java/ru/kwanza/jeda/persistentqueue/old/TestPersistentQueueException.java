package ru.kwanza.jeda.persistentqueue.old;

import junit.framework.TestCase;
import ru.kwanza.jeda.persistentqueue.PersistenceQueueException;

/**
 * @author Guzanov Alexander
 */
public class TestPersistentQueueException extends TestCase {
    public void test() {
        PersistenceQueueException e4 = new PersistenceQueueException(new RuntimeException());
        assertEquals(e4.getMessage(), "java.lang.RuntimeException");
        assertEquals(e4.getCause().getClass(), RuntimeException.class);
    }
}
