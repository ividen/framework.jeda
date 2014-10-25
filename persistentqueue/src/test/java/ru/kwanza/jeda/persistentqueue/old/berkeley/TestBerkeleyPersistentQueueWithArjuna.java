package ru.kwanza.jeda.persistentqueue.old.berkeley;

/**
 * @author Guzanov Alexander
 */
public class TestBerkeleyPersistentQueueWithArjuna extends TestBerkeleyPersistentQueue {
    @Override
    protected String getContextName() {
        return "berkeley-persistentqueue-config-arjuna.xml";
    }

}
