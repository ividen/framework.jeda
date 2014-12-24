package ru.kwanza.jeda.timerservice.entitytimer;

import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import ru.kwanza.jeda.api.entitytimer.IEntityTimerManager;
import ru.kwanza.jeda.timerservice.entitytimer.entity.EntityWithTimer1;
import ru.kwanza.jeda.timerservice.entitytimer.entity.EntityWithTimer2;
import ru.kwanza.jeda.timerservice.entitytimer.entity.EntityWithTimer3Child;
import ru.kwanza.jeda.timerservice.entitytimer.entity.EntityWithTimer4Child;

import static junit.framework.Assert.*;

import javax.annotation.Resource;

/**
 * @author Michael Yeskov
 */

@ContextConfiguration(locations = "test1-config.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TestEntityTimerManager extends AbstractJUnit4SpringContextTests {

    private static final long SLEEP = 1000;

    @Resource(name = "jeda.IEntityTimerManager")
    private IEntityTimerManager timerManager;

    @Test
    public void testTimerManager1() throws Exception {
        EntityWithTimer1 entityWithTimer1 = new EntityWithTimer1();
        EntityWithTimer2 entityWithTimer2 = new EntityWithTimer2();
        EntityWithTimer3Child entityWithTimer3Child = new EntityWithTimer3Child();
        EntityWithTimer4Child entityWithTimer4Child = new EntityWithTimer4Child();

        assertEquals(false, timerManager.isActive(entityWithTimer1));
        assertEquals(false, timerManager.isActive("default", entityWithTimer1));
        assertEquals(false, timerManager.isActive(entityWithTimer3Child));
        //1-8
        for (int i = 1; i <= 8; i++){
            assertEquals(false, timerManager.isActive("timer" + i, entityWithTimer4Child));
        }


        //
        timerManager.registerInfiniteTimer(entityWithTimer1);
        timerManager.registerInfiniteTimer("default", entityWithTimer2);

        assertEquals(true, timerManager.isActive(entityWithTimer1));
        assertEquals(true, timerManager.isActive(entityWithTimer2));
        assertEquals(Long.valueOf(EntityTimerManager.INFINITE_TIMER_VALUE), entityWithTimer1.timer);
        assertEquals(Long.valueOf(EntityTimerManager.INFINITE_TIMER_VALUE), entityWithTimer2.internalGetTimer());

        timerManager.interruptTimer(entityWithTimer1, entityWithTimer2);

        assertEquals(false, timerManager.isActive(entityWithTimer1));
        assertEquals(false, timerManager.isActive(entityWithTimer2));
        assertEquals(null, entityWithTimer1.timer);
        assertEquals(null, entityWithTimer2.internalGetTimer());

        //

        timerManager.registerTimer(SLEEP, new Object[]{entityWithTimer1, entityWithTimer2, entityWithTimer3Child});
        assertEquals(true, timerManager.isActive(entityWithTimer1));
        assertEquals(true, timerManager.isActive(entityWithTimer2));
        assertEquals(true, timerManager.isActive(entityWithTimer3Child));

        Thread.sleep(SLEEP + 1);

        assertNotNull(entityWithTimer1.timer);
        assertNotNull(entityWithTimer2.internalGetTimer());
        assertNotNull(entityWithTimer3Child.internalGetTimer());

        assertEquals(false, timerManager.isActive(entityWithTimer1));
        assertEquals(false, timerManager.isActive(entityWithTimer2));
        assertEquals(false, timerManager.isActive(entityWithTimer3Child));

        assertNull(entityWithTimer1.timer);
        assertNull(entityWithTimer2.internalGetTimer());
        assertNull(entityWithTimer3Child.internalGetTimer());

        //

        long expireTime =  System.currentTimeMillis() + SLEEP;
        for (int i = 1; i <= 8; i++){
            assertEquals(false, timerManager.isActive("timer" + i, entityWithTimer4Child));
            timerManager.registerTimerWithExpireTime("timer" + i, expireTime, entityWithTimer4Child);
            assertEquals(true, timerManager.isActive("timer" + i, entityWithTimer4Child));
        }

        assertNotNull(entityWithTimer4Child.getTimer3());
        assertNotNull(entityWithTimer4Child.getTimer4());
        assertNotNull(entityWithTimer4Child.getTimer5());
        assertNotNull(entityWithTimer4Child.getTimer8());

        timerManager.interruptTimer("timer3", entityWithTimer4Child);
        timerManager.interruptTimer("timer4", entityWithTimer4Child);

        assertNull(entityWithTimer4Child.getTimer3());
        assertNull(entityWithTimer4Child.getTimer4());

        assertEquals(false, timerManager.isActive("timer3", entityWithTimer4Child));
        assertEquals(false, timerManager.isActive("timer4", entityWithTimer4Child));

        assertEquals(true, timerManager.isActive("timer1", entityWithTimer4Child));

        Thread.sleep(SLEEP + 1);

        for (int i = 1; i <= 8; i++){
            assertEquals(false, timerManager.isActive("timer" + i, entityWithTimer4Child));
        }
    }


}
