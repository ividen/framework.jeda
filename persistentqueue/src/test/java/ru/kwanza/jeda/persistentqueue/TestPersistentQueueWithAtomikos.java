package ru.kwanza.jeda.persistentqueue;

/**
 * @author Guzanov Alexander
 */
public class TestPersistentQueueWithAtomikos extends TestPersistentQueue {
    @Override
    protected String getContextName() {
        return "application-context-atomikos.xml";
    }
}
