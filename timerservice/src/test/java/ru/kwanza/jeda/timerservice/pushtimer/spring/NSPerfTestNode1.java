package ru.kwanza.jeda.timerservice.pushtimer.spring;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.pushtimer.manager.ITimerManager;
import ru.kwanza.jeda.timerservice.pushtimer.common.Inserter;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClassRepository;
import ru.kwanza.jeda.timerservice.pushtimer.memory.FiredTimersStorageRepository;


import javax.annotation.Resource;
import java.util.concurrent.*;

/**
 * @author Michael Yeskov
 */

@ContextConfiguration(locations = "ns-test-node1-config.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Ignore
public class NSPerfTestNode1 extends AbstractJUnit4SpringContextTests {

    @Resource
    private IJedaManager jedaManager;

    @Resource
    private ITimerManager timerManager;

    @Resource
    private FiredTimersStorageRepository firedTimersStorageRepository;

    @Resource
    private TimerClassRepository timerClassRepository;

    private static final long NODE_ID = 1;

    @Test
    public void multiThreadedTest() throws Exception {
        Thread.sleep(1000);
        long startTime = System.currentTimeMillis();
        System.out.println("MultiThreadedTest start");

        BlockingQueue q = new ArrayBlockingQueue(1000);
        Executor executor = new ThreadPoolExecutor(20, 20, 10000, TimeUnit.MILLISECONDS , q);

        for (int i =1 ; i <= 7; i++) {
            executor.execute(new Inserter("TIMER_" + i ,jedaManager, timerManager, i * 10 + NODE_ID, true));
        }


        for (int i=0; i<100000; i++) {
            Thread.sleep(1000);
        }
        //Thread.sleep(1000000000);


        System.out.println("MultiThreadedTest end. Time=" + (System.currentTimeMillis() - startTime));
    }
}
