package ru.kwanza.jeda.persistentqueue;

/**
 * @author Guzanov Alexander
 */
public class TestPersistentQueueWithSpring extends TestPersistentQueue {
    @Override
    protected String getContextName() {
        return "application-context-ds.xml";
    }
}
