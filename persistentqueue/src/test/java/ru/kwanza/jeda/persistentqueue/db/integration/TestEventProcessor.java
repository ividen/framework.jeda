package ru.kwanza.jeda.persistentqueue.db.integration;

import org.springframework.beans.factory.annotation.Autowired;
import ru.kwanza.jeda.api.IEventProcessor;
import ru.kwanza.jeda.api.IJedaManager;

import java.util.Collection;

/**
 * @author Alexander Guzanov
 */
public class TestEventProcessor implements IEventProcessor {
//    @Resource(name="testStage1")
//    private ISink<TestEvent> sink1;
    @Autowired
    private IJedaManager manager;

    public void process(Collection events) {
//        System.out.println(manager.getCurrentStage().getName() + ":" + events.size());
//        try {
//            sink1.tryPut(events);
//        }catch (Exception e){
//            throw new RuntimeException(e);
//        }
    }
}
