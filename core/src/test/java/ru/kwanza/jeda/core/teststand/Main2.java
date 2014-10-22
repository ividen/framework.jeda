package ru.kwanza.jeda.core.teststand;

import ru.kwanza.jeda.api.*;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.internal.IJedaManagerInternal;
import ru.kwanza.jeda.core.manager.DefaultJedaManager;
import ru.kwanza.jeda.core.queue.ObjectCloneType;
import ru.kwanza.jeda.core.queue.TransactionalMemoryQueue;
import ru.kwanza.jeda.core.resourcecontroller.SmartResourceController;
import ru.kwanza.jeda.core.stage.Stage;
import ru.kwanza.jeda.core.threadmanager.shared.SharedThreadManager;
import ru.kwanza.jeda.core.threadmanager.shared.comparator.InputRateAndWaitingTimeComparator;
import ru.kwanza.jeda.core.threadmanager.stage.StageThreadManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class Main2 {
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
            for (Event e : events) {
                sum += Long.valueOf(e.getContextId());
            }

            System.out.println(name + ":" + sum);
        }
    }

    public static class InputThread1 extends Thread {
        private final IJedaManager manager;

        public InputThread1(IJedaManager manager) {
            super("InputThread1");
            this.manager = manager;
        }

        public void run() {
            int j = 0;
            while (true) {
                try {
                    sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int count = 15000;
                ArrayList<Event> events = new ArrayList<Event>();
                for (int i = 0; i < count; i++) {
                    events.add(new Event(String.valueOf(i)));
                }

                try {
                    manager.getStage("TestStage-" + j).<Event>getSink().put(events);
                } catch (SinkException e) {
                    e.printStackTrace();
                }

                j++;
                if (j >= 10) {
                    j = 0;
                }
            }
        }
    }

    public static class InputThread2 extends Thread {
        private IJedaManager manager;

        public InputThread2(IJedaManager manager) {
            super("InputThread2");
            this.manager = manager;
        }

        public void run() {
            int j = 10;
            while (true) {
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int count = 5000;
                ArrayList<Event> events = new ArrayList<Event>();
                for (int i = 0; i < count; i++) {
                    events.add(new Event(String.valueOf(i)));
                }

                try {
                    manager.getStage("TestStage-" + j).<Event>getSink().put(events);
                } catch (SinkException e) {
                    e.printStackTrace();
                }

                j++;
                if (j >= 15) {
                    j = 0;
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-context.xml", Main.class);
        DefaultJedaManager systemManager = ctx.getBean("ru.kwanza.jeda.api.IJedaManager", DefaultJedaManager.class);
        SharedThreadManager stageThreadManager = new SharedThreadManager("testThreads", systemManager);
        stageThreadManager.setMaxThreadCount(10);
        InputRateAndWaitingTimeComparator stageComparator = new InputRateAndWaitingTimeComparator();
        stageComparator.setMaxWaitingTime(10 * 1000);
        stageThreadManager.setStageComparator(stageComparator);

        for (int i = 0; i < 10; i++) {
            SmartResourceController resourceController = new SmartResourceController();
            resourceController.setMaxBatchSize(100000);
            TransactionalMemoryQueue queue = new TransactionalMemoryQueue(ctx.getBean("ru.kwanza.jeda.api.IJedaManager", IJedaManager.class),
                    ObjectCloneType.SERIALIZE, Long.MAX_VALUE);
            Stage testStage = new Stage(systemManager, "TestStage-" + i,
                    new EventProcessor("TestStage-" + i), queue, stageThreadManager, null, resourceController,  true);

            systemManager.registerStage(testStage);
        }


        StageThreadManager threadManager = new StageThreadManager("sdfsdf", systemManager);
        for (int i = 10; i < 15; i++) {
            SmartResourceController resourceController = new SmartResourceController();
            resourceController.setMaxBatchSize(100000);

            TransactionalMemoryQueue queue = new TransactionalMemoryQueue(
                    systemManager,
                    ObjectCloneType.SERIALIZE, Long.MAX_VALUE);
            Stage testStage = new Stage(systemManager, "TestStage-" + i, new EventProcessor("TestStage-" + i), queue,
                    stageThreadManager, null, resourceController,  true);

            systemManager.registerStage(testStage);
        }

        new InputThread1(systemManager).start();
        new InputThread2(systemManager).start();
        Thread.currentThread().join();
    }
}