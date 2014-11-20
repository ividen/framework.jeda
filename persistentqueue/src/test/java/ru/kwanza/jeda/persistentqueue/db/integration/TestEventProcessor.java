package ru.kwanza.jeda.persistentqueue.db.integration;

import ru.kwanza.jeda.api.IEventProcessor;

import java.util.Collection;

/**
 * @author Alexander Guzanov
 */
public class TestEventProcessor implements IEventProcessor {
    public void process(Collection events) {
        System.out.println(events.size());
    }
}
