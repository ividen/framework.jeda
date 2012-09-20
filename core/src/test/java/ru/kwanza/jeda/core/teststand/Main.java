package ru.kwanza.jeda.core.teststand;

import ru.kwanza.jeda.api.*;
import ru.kwanza.jeda.api.internal.ISystemManager;
import ru.kwanza.jeda.core.manager.DefaultSystemManager;
import ru.kwanza.jeda.core.queue.ObjectCloneType;
import ru.kwanza.jeda.core.queue.TransactionalMemoryQueue;
import ru.kwanza.jeda.core.resourcecontroller.SmartResourceController;
import ru.kwanza.jeda.core.stage.Stage;
import ru.kwanza.jeda.core.threadmanager.stage.StageThreadManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class Main {
    public static class Event  extends AbstractEvent {
        private String contextId;

        public Event(String contextId) {
            this.contextId = contextId;
        }

        public String getContextId() {
            return contextId;
        }

        @Override
        public String toString() {
            return "Event{" +
                    "contextId='" + contextId + '\'' +
                    '}';
        }
    }

    public static class EventProcessor implements IEventProcessor<Event> {
        String name;

        public EventProcessor(String name) {
            this.name = name;
        }

        public void process(Collection<Event> events) {
            long sum = 0;
           if(events.size()>30000){
               try {
                   Thread.sleep(1000);
               } catch (InterruptedException e) {
                   e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
               }
           }
            for (Event e : events) {
                sum += Long.valueOf(e.getContextId());
            }

//            System.out.println(name + ":" + sum);
        }
    }

    public static class InputThread extends Thread {
        public InputThread() {
            super("InputThread");
        }

        public void run() {
            int j = 0;
            while (true) {


                int count = 15000;
                ArrayList<Event> events = new ArrayList<Event>();
                for (int i = 0; i < count; i++) {
                    events.add(new Event(String.valueOf(i)));
                }

                try {
                    Manager.getStage("TestStage-" + j).<Event>getSink().put(events);
                } catch (SinkException e) {
                    e.printStackTrace();
                }

                j++;
                if (j >= 1) {
                    j = 0;
                    try {
//                        sleep(1000);
                        sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-context.xml", Main.class);
        DefaultSystemManager systemManager = ctx.getBean("ru.kwanza.jeda.api.internal.ISystemManager", DefaultSystemManager.class);
//        SharedThreadManager stageThreadManager = new SharedThreadManager("testThreads", systemManager);
//        stageThreadManager.setMaxThreadCount(1);
        for (int i = 0; i < 1; i++) {
//            FixedBatchSizeResourceController resourceController = new FixedBatchSizeResourceController(500000);
//            resourceController.setMaxThreadCount(8);
//            resourceController.setWaitForFillingTimeout(1000);
            SmartResourceController resourceController = new SmartResourceController(10);
            resourceController.setMaxBatchSize(50000);
            resourceController.setMaxElementCount(1000);
            resourceController.setWaitForFillingTimeout(3000);
            TransactionalMemoryQueue queue = new TransactionalMemoryQueue(
                    ctx.getBean("ru.kwanza.jeda.api.internal.ISystemManager", ISystemManager.class),
                    ObjectCloneType.SERIALIZE, Long.MAX_VALUE);

            StageThreadManager testThread = new StageThreadManager("testThread", systemManager);
            Stage testStage = new Stage(systemManager, "TestStage-" + i, new EventProcessor("TestStage-" + i),
                    queue, testThread, null, resourceController, true);
            systemManager.registerStage(testStage);
        }

        new InputThread().start();
        Thread.currentThread().join();
    }
}
