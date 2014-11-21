package ru.kwanza.jeda.persistentqueue.db.integration;

import org.springframework.beans.factory.annotation.Autowired;
import ru.kwanza.jeda.api.IEventProcessor;
import ru.kwanza.jeda.api.IJedaManager;

import java.util.Collection;

/**
 * @author Alexander Guzanov
 */
public class TestEventProcessorNext implements IEventProcessor {
    @Autowired
    private IJedaManager manager;

    public void process(Collection events) {
//        System.out.println(manager.getCurrentStage().getName() + ":" + events.size());
    }
}
