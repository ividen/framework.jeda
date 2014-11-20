package ru.kwanza.jeda.persistentqueue.db.integration;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.kwanza.jeda.api.ISink;
import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.clusterservice.IClusterService;
import ru.kwanza.txn.api.spi.ITransactionManager;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Alexander Guzanov
 */
public class TestPersistentQueue {

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-integration-config.xml", TestPersistentQueue.class);
        AtomicLong id = new AtomicLong(0l);


        ISink<TestEvent>  sink = (ISink<TestEvent>) ctx.getBean("testStage");
        IClusterService  service = ctx.getBean(IClusterService.class);
        ITransactionManager  tm = (ITransactionManager) ctx.getBean("txn.ITransactionManager");
        Thread.sleep(5000);
        for (int i = 0; i < 100000; i++) {
            ArrayList<TestEvent> events = new ArrayList<TestEvent>();
            for (int j = 0; j < 10000; j++) {
                long l = id.incrementAndGet();
                l = l*10+service.getCurrentNode().getId();
                events.add(new TestEvent(l, String.valueOf(j)));
            }
            tm.begin();

            try {
                sink.tryPut(events);
            } catch (SinkException e) {
                e.printStackTrace();
            }finally {
                tm.commit();
            }
        }

        System.out.println("Finish publishing!");
        Thread.currentThread().join();
    }
}
