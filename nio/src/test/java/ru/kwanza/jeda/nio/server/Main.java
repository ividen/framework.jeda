package ru.kwanza.jeda.nio.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Guzanov Alexander
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("application.xml", Main.class);

        Thread.currentThread().join();
    }
}
