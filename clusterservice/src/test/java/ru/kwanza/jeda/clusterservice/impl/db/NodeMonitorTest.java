package ru.kwanza.jeda.clusterservice.impl.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.kwanza.jeda.clusterservice.IClusterService;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;

public class NodeMonitorTest /* extends TestCase*/ {
    private static final Logger logger = LoggerFactory.getLogger(NodeMonitorTest.class);

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-integration-config.xml", NodeMonitorTest.class);


        IClusterService bean = ctx.getBean(IClusterService.class);

        final TestComponent critical = (TestComponent) ctx.getBean("critical");

        while (true) {
            try {
                bean.criticalSection(critical, new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {

                        logger.info(System.getProperty("jeda.clusterservice.nodeId") + "-test");
                        return null;
                    }
                });
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (ComponentInActiveExcetion componentInActiveExcetion) {
                componentInActiveExcetion.printStackTrace();
            }
            Thread.sleep(1000);
        }

    }

}
