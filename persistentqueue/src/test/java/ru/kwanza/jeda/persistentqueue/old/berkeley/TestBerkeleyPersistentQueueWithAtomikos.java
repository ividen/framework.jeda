package ru.kwanza.jeda.persistentqueue.old.berkeley;

/**
 * @author Guzanov Alexander
 */
public class TestBerkeleyPersistentQueueWithAtomikos extends TestBerkeleyPersistentQueue {
    @Override
    protected String getContextName() {
        return "berkeley-persistentqueue-config-atomikos.xml";
    }

}