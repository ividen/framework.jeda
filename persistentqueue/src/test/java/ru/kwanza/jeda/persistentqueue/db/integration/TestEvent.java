package ru.kwanza.jeda.persistentqueue.db.integration;

import ru.kwanza.jeda.persistentqueue.DefaultPersistableEvent;

/**
 * @author Alexander Guzanov
 */
public class TestEvent extends DefaultPersistableEvent {
    private String name;

    public TestEvent(Long persistId, String name) {
        super(persistId);
        this.name = name;
    }

    public TestEvent(String name) {
        this.name = name;
    }
}
