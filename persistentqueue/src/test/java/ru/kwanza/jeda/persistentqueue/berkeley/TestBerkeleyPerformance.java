package ru.kwanza.jeda.persistentqueue.berkeley;

import ru.kwanza.jeda.api.Manager;
import ru.kwanza.jeda.clusterservice.old.impl.mock.MockClusterServiceImpl;
import ru.kwanza.jeda.jeconnection.JEConnectionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;

/**
 * @author Guzanov Alexander
 */
public class TestBerkeleyPerformance {

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                "classpath:com/intervale/jeda/persistentqueue/berkeley/application-config.xml");

        JEConnectionFactory berkeleyFactory = ctx.getBean("berkeleyFactory", JEConnectionFactory.class);

        Manager.getTM().begin();
//        JEConnection connection = berkeleyFactory.getConnection(0l);


        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();

        Thread.sleep(10000);
        for (int j = 0; j < 100000; j++) {
            ArrayList<TestEvent> events = new ArrayList<TestEvent>();
            for (int i = 0; i < 1000; i++) {
                events.add(new TestEvent(String.valueOf(i * j)));
            }

            Manager.getTM().begin();
            try {
                Manager.getStage("Stage-1").<TestEvent>getSink().put(events);
                Manager.getTM().commit();
            } catch (Throwable e) {
                Manager.getTM().rollback();
            }
            System.out.println("�������� " + j);
            Thread.sleep(100);
        }


    }
}
