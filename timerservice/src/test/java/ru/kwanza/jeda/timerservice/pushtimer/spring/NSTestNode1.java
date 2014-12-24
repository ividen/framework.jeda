package ru.kwanza.jeda.timerservice.pushtimer.spring;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.IStage;
import ru.kwanza.jeda.api.pushtimer.manager.ITimerManager;
import ru.kwanza.jeda.api.pushtimer.manager.NewTimer;


import javax.annotation.Resource;
import java.util.Arrays;

/**
 * @author Michael Yeskov
 */

@ContextConfiguration(locations = "ns-test-node1-config.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class NSTestNode1 extends AbstractJUnit4SpringContextTests {

    @Resource
    private IJedaManager jedaManager;

    @Resource
    private ITimerManager timerManager;

    @Test
    @Ignore
    public void simpleConfigTest() throws Exception {
        Thread.sleep(1000);

        IStage stage1 = jedaManager.getStage("TIMER_1");
        IStage stage2 = jedaManager.getStage("TIMER_2");
        IStage stage3 = jedaManager.getStage("TIMER_3");
        IStage stage4 = jedaManager.getStage("TIMER_4");
        IStage stage5 = jedaManager.getStage("TIMER_5");
        IStage stage6 = jedaManager.getStage("TIMER_6");
        IStage stage7 = jedaManager.getStage("TIMER_7");

        jedaManager.getTransactionManager().begin();
        timerManager.scheduleTimers(Arrays.asList(new NewTimer("TIMER_1", "1", 10)));
        jedaManager.getTransactionManager().commit();

        jedaManager.getTransactionManager().begin();
        timerManager.scheduleTimers(Arrays.asList(
                new NewTimer("TIMER_1", "2", 10),
                new NewTimer("TIMER_2", "1", 10),
                new NewTimer("TIMER_3", "1", 10),
                new NewTimer("TIMER_4", "1", 10),
                new NewTimer("TIMER_5", "1", 10),
                new NewTimer("TIMER_6", "1", 10),
                new NewTimer("TIMER_7", "1", 10)
        ));
        jedaManager.getTransactionManager().commit();


        System.out.println("done");
        Thread.sleep(10000000);

    }
}
