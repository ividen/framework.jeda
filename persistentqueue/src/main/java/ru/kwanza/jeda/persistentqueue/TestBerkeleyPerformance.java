package ru.kwanza.jeda.persistentqueue;

import ru.kwanza.jeda.api.Manager;
import ru.kwanza.jeda.clusterservice.old.impl.mock.MockClusterServiceImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;

/**
 * @author Guzanov Alexander
 */
public class TestBerkeleyPerformance {

    public static void main(String[] args) {
        int iterationCount = Integer.valueOf(args[0]);
        int size = Integer.valueOf(args[1]);
        int nodeId = Integer.valueOf(args[2]);
        MockClusterServiceImpl.getInstance().setNodeId(nodeId);


        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                "application-conf.xml", TestBerkeleyPerformance.class);

        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();


        TestEventProcessor.expectedSize.set(iterationCount * size);

        long start = 0;
        long finish = 0;

        double reqsec = 0;
        double sum = 0;

        for (int i = 0; i < iterationCount; i++) {
            ArrayList<TestEvent> list = new ArrayList<TestEvent>(size);
            for (int j = 0; j < size; j++) {
                list.add(new TestEvent(String.valueOf(j) + "-" + String.valueOf(i)));
            }

            start = System.currentTimeMillis();

            Manager.getTM().begin();
            try {
                Manager.getStage("TestStage30").<TestEvent>getSink().put(list);
                Manager.getTM().commit();
            } catch (Throwable e) {
                e.printStackTrace();
                Manager.getTM().rollback();
            }

            finish = System.currentTimeMillis();
            reqsec = size / (((double) (finish - start)) / 1000);
            sum = sum + reqsec;
            System.out.println(reqsec);
        }

        double res = sum / iterationCount;
        System.out.println("Res: " + res);


    }
}
