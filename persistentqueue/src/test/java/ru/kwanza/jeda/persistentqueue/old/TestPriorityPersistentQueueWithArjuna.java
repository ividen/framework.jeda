package ru.kwanza.jeda.persistentqueue.old;

/**
 * @author Guzanov Alexander
 */
public class TestPriorityPersistentQueueWithArjuna extends TestPriorityPersistentQueue {
    @Override
    protected String getContextName() {
        return "application-context-arjuna.xml";
    }
}
