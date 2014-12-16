package ru.kwanza.jeda.timerservice.pushtimer.spring;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.IStage;
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

@ContextConfiguration(locations = "ns-test-config.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class NSTest extends AbstractJUnit4SpringContextTests {

    @Resource
    private IJedaManager jedaManager;

    @Test
    public void simpleConfigTest() throws Exception {
        IStage stage1 = jedaManager.getStage("DEFAULT_TIMER");
      //  IStage stage2 = jedaManager.getStage("DEFAULT_TIMER_2");
        System.out.println("done");

    }
}
