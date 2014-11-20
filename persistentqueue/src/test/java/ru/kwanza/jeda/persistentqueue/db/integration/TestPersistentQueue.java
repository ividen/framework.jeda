package ru.kwanza.jeda.persistentqueue.db.integration;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.kwanza.jeda.clusterservice.IClusterService;

/**
 * @author Alexander Guzanov
 */
public class TestPersistentQueue {

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-integration-config.xml", NodeMonitorTest.class);



        IClusterService bean = ctx.getBean(IClusterService.class);

        Thread.currentThread().join();
    }
}
