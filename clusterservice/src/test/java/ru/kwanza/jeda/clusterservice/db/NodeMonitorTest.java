package ru.kwanza.jeda.clusterservice.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.kwanza.jeda.clusterservice.IClusterService;
import ru.kwanza.jeda.clusterservice.IClusteredModule;
import ru.kwanza.jeda.clusterservice.Node;

public class NodeMonitorTest /* extends TestCase*/ {
    private static final Logger logger = LoggerFactory.getLogger(NodeMonitorTest.class);

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-context.xml", NodeMonitorTest.class);




        IClusterService bean = ctx.getBean(IClusterService.class);

        bean.registerModule(new IClusteredModule() {
            int counter = 10;
            public String getName() {
                return "Test";
            }

            public void handleStart() {
                System.out.println("Started");
            }

            public void handleStop() {
                System.out.print("Stoped");
            }

            public boolean handleRepair(Node node) {
                System.out.println("REpair " + node.getId());
                return counter-- <0;
            }
        });

        Thread.currentThread().join();
    }
}
