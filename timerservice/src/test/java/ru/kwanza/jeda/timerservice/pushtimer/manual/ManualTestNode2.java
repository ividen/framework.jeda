package ru.kwanza.jeda.timerservice.pushtimer.manual;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.timerservice.pushtimer.manager.ITimerManager;
import ru.kwanza.jeda.timerservice.pushtimer.StatisticsCalculator;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClassRepository;
import ru.kwanza.jeda.timerservice.pushtimer.memory.FiredTimersMemoryStorage;
import ru.kwanza.jeda.timerservice.pushtimer.memory.FiredTimersStorageRepository;


import javax.annotation.Resource;
import java.util.concurrent.*;

/**
 * @author Michael Yeskov
 */

@ContextConfiguration(locations = "node2-test-config.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Ignore
public class ManualTestNode2 extends AbstractJUnit4SpringContextTests {

    @Resource
    private IJedaManager jedaManager;

    @Resource
    private ITimerManager timerManager;

    @Resource
    private FiredTimersStorageRepository firedTimersStorageRepository;

    @Resource
    private TimerClassRepository timerClassRepository;

    private static final String timerName = "DEFAULT_TIMER";

    private static final long NODE_ID = 2;

    @Test
    @Ignore
    public void multiThreadedTest() throws Exception {
        Thread.sleep(1000);
        long startTime = System.currentTimeMillis();
        System.out.println("MultiThreadedTest start");

        BlockingQueue q = new ArrayBlockingQueue(1000);
        Executor executor = new ThreadPoolExecutor(10, 10, 10000, TimeUnit.MILLISECONDS , q);

        StatisticsCalculator.insert.firstStart();
        StatisticsCalculator.process.firstStart();

        for (int i =0 ; i < 4; i++) {
            executor.execute(new Inserter(jedaManager, timerManager, i * 10 + NODE_ID));
        }

        FiredTimersMemoryStorage storage = firedTimersStorageRepository.getFiredTimersStorage(timerClassRepository.getClassByTimerName(timerName));

        for (int i=0; i<100000000; i++) {
            StatisticsCalculator.insert.start();
            StatisticsCalculator.process.start();
            Thread.sleep(10000);
         //   StatisticsCalculator.insert.stopAndPrint();
         //   StatisticsCalculator.process.stopAndPrint();
        }

        System.out.println("MultiThreadedTest end. Time=" + (System.currentTimeMillis() - startTime));
    }
}
