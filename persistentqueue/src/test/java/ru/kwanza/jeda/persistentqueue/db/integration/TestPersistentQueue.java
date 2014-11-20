package ru.kwanza.jeda.persistentqueue.db.integration;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Alexander Guzanov
 */
public class TestPersistentQueue {

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-integration-config.xml", TestPersistentQueue.class);



        Thread.currentThread().join();
    }
}
