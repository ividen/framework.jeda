package ru.kwanza.jeda.persistentqueue.old;

/**
 * @author Guzanov Alexander
 */
public class TestPriorityPersistentQueueWithAtomikos extends TestPriorityPersistentQueue {
    @Override
    protected String getContextName() {
        return "application-context-atomikos.xml";
    }
}