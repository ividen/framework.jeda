package ru.kwanza.jeda.persistentqueue.berkeley;

import org.springframework.test.context.ContextConfiguration;

/**
 * @author Guzanov Alexander
 */
@ContextConfiguration(locations = "berkeley-persistentqueue-config-atomikos.xml")
public class TestBerkeleyPersistenceControllerWithAtomikos extends TestBerkeleyPersistenceController {
}
