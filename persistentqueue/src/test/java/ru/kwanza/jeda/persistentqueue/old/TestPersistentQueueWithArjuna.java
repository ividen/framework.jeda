package ru.kwanza.jeda.persistentqueue.old;

/**
 * @author Guzanov Alexander
 */
public class TestPersistentQueueWithArjuna extends TestPersistentQueue {
    @Override
    protected String getContextName() {
        return "application-context-arjuna.xml";
    }
}
