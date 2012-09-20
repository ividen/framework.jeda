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
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Guzanov Alexander
 */
public class StandWithErrorControllerCheck {
    public static class Event  extends AbstractEvent implements IPriorityEvent {
        private static final AtomicLong ids = new AtomicLong(0l);

        private long id = ids.incrementAndGet();
        private String contextId;

        public Event(String contextId) {
            this.contextId = contextId;
        }

        public String getContextId() {
            if (contextId == null) {
                throw new NullPointerException();
            }
            return contextId;
        }

        public Priority getPriority() {
            return contextId == null ? Priority.CRITICAL : Priority.NORMAL;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Event event = (Event) o;

            if (id != event.id) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return (int) (id ^ (id >>> 32));
        }

        @Override
        public String toString() {
            return "Event{" +
                    "id=" + id +
                    ", contextId='" + contextId + '\'' +
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

    public static class InputThread extends Thread {
        long counter = 0;
        long FREQ = 50000;

        public InputThread() {
            super("InputThread");
        }

        public void run() {
            while (true) {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

                int count = 15000;
                ArrayList<Event> events = new ArrayList<Event>();
                for (int i = 0; i < count; i++) {
                    counter++;
                    events.add(new Event(counter % FREQ == 0 ? null : String.valueOf(counter)));
                }

                try {
                    Manager.getStage("TestStage").<Event>getSink().put(events);
                } catch (SinkException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-context.xml", Main.class);
        DefaultSystemManager systemManager = ctx.getBean("ru.kwanza.jeda.api.internal.ISystemManager",
                DefaultSystemManager.class);
        StageThreadManager stageThreadManager = new StageThreadManager("testThreads", systemManager);
        stageThreadManager.setMaxThreadCount(10);

        SmartResourceController resourceController = new SmartResourceController();
        resourceController.setMaxBatchSize(50000);
        TransactionalMemoryQueue queue = new TransactionalMemoryQueue(
                ctx.getBean("ru.kwanza.jeda.api.internal.ISystemManager", ISystemManager.class),
                ObjectCloneType.SERIALIZE, Long.MAX_VALUE);
        Stage testStage = new Stage(systemManager, "TestStage", new EventProcessor("TestStage"), queue,
                stageThreadManager, null, resourceController, true);


        systemManager.registerStage(testStage);

        new InputThread().start();
        Thread.currentThread().join();
    }
}