package ru.kwanza.jeda.persistentqueue.berkeley;

/**
 * @author Guzanov Alexander
 */
public class TestBerkeleyPersistenceControllerWithAtomikos extends TestBerkeleyPersistenceController {
    @Override
    protected String getContextName() {
        return "berkeley-persistentqueue-config-atomikos.xml";
    }
}
