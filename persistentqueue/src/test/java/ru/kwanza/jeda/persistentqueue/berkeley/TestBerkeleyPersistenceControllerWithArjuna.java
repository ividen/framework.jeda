package ru.kwanza.jeda.persistentqueue.berkeley;

import org.springframework.test.context.ContextConfiguration;

/**
 * @author Guzanov Alexander
 */
@ContextConfiguration(locations = "berkeley-persistentqueue-config-arjuna.xml")
public class TestBerkeleyPersistenceControllerWithArjuna extends TestBerkeleyPersistenceController {
}
