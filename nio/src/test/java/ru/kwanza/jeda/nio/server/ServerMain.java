package ru.kwanza.jeda.nio.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Guzanov Alexander
 */
public class ServerMain {

    public static void main(String[] args) throws InterruptedException {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("application.xml", ServerMain.class);

        Thread.currentThread().join();
    }
}
