package ru.kwanza.jeda.persistentqueue;

/**
 * @author Guzanov Alexander
 */
public class TestPersistentQueueWithArjuna extends TestPersistentQueue {
    @Override
    protected String getContextName() {
        return "application-context-arjuna.xml";
    }
}
