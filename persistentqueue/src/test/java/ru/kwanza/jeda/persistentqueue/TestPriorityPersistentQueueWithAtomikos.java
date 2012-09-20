package ru.kwanza.jeda.persistentqueue;

/**
 * @author Guzanov Alexander
 */
public class TestPriorityPersistentQueueWithAtomikos extends TestPriorityPersistentQueue {
    @Override
    protected String getContextName() {
        return "application-context-atomikos.xml";
    }
}