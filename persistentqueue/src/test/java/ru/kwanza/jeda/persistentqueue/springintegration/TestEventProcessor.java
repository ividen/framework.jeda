package ru.kwanza.jeda.persistentqueue.springintegration;

import ru.kwanza.jeda.api.IEventProcessor;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class TestEventProcessor implements IEventProcessor {
    public void process(Collection events) {
//        try {
//            Thread.sleep(10000000l);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        System.out.println(events);
    }
}
