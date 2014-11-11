package ru.kwanza.jeda.clusterservice.impl.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.kwanza.jeda.clusterservice.IClusterService;

public class NodeMonitorTest /* extends TestCase*/ {
    private static final Logger logger = LoggerFactory.getLogger(NodeMonitorTest.class);

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-config.xml", NodeMonitorTest.class);



        IClusterService bean = ctx.getBean(IClusterService.class);

        Thread.currentThread().join();
    }
}
