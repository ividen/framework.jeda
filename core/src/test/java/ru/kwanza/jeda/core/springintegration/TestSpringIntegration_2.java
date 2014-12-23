package ru.kwanza.jeda.core.springintegration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.kwanza.jeda.api.*;
import ru.kwanza.jeda.api.internal.IJedaManagerInternal;
import ru.kwanza.jeda.api.internal.IStageInternal;
import ru.kwanza.jeda.core.manager.SystemContextController;
import ru.kwanza.jeda.core.manager.SystemFlowBus;
import ru.kwanza.jeda.core.manager.SystemTimer;
import ru.kwanza.jeda.core.threadmanager.shared.SharedThreadManager;
import ru.kwanza.jeda.core.threadmanager.shared.comparator.*;

import static junit.framework.Assert.*;

/**
 * @author Guzanov Alexander
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration("application-config_1.xml")
public class TestSpringIntegration_2 {

    @Autowired
    private ApplicationContext ctx;

    @Test
    public void test2() {
        IJedaManagerInternal manager = (IJedaManagerInternal) ctx.getBean("jeda.IJedaManager");

        assertEquals(manager.getFlowBus("CPAReqFlowBus").getClass(), SystemFlowBus.class);
        assertEquals(manager.<Object, IContext>getContextController("TestContext").getClass(), SystemContextController.class);
        assertEquals(manager.getTimer("TestTimer").getClass(), SystemTimer.class);

        assertEquals("CPAReqFlowBus", manager.resolveObjectName(manager.getFlowBus("CPAReqFlowBus")));
        assertEquals(ctx.getBean("CPAReqFlowBus"), manager.getFlowBus("CPAReqFlowBus"));
        assertEquals("CPAResFlowBus", manager.resolveObjectName(manager.getFlowBus("CPAResFlowBus")));
        assertEquals(ctx.getBean("CPAResFlowBus"), manager.getFlowBus("CPAResFlowBus"));

        assertEquals("TestContext", manager.resolveObjectName(manager.<Object, IContext>getContextController("TestContext")));
        assertEquals(ctx.getBean("TestContext"), manager.<Object, IContext>getContextController("TestContext"));
        assertEquals("TestTimer", manager.resolveObjectName(manager.getTimer("TestTimer")));
        assertEquals(ctx.getBean("TestTimer"), manager.getTimer("TestTimer"));

        IStageInternal stage = manager.getStageInternal("TestStage20");
        manager.getStage("TestStage20");

        assertEquals(stage.getName(), "TestStage20");
        assertEquals(manager.resolveObjectName(stage), "TestStage20");
        assertEquals(manager.resolveObjectName(stage.getSink()), "TestStage20");

        assertEquals(((SharedThreadManager) ctx.getBean("ProtocolSchaedPool")).getStageComparator().getClass(),
                InputRateComparator.class);
        assertEquals(((SharedThreadManager) ctx.getBean("ProtocolSchaedPool1")).getStageComparator().getClass(),
                ThreadCountComparator.class);
        assertEquals(((SharedThreadManager) ctx.getBean("ProtocolSchaedPool2")).getStageComparator().getClass(),
                WaitingTimeComparator.class);
        assertEquals(((SharedThreadManager) ctx.getBean("ProtocolSchaedPool3")).getStageComparator().getClass(), RoundRobinComparator.class);
        assertEquals(((SharedThreadManager) ctx.getBean("ProtocolSchaedPool4")).getStageComparator().getClass(),
                InputRateAndWaitingTimeComparator.class);
        assertEquals(((SharedThreadManager) ctx.getBean("ProtocolSchaedPool5")).getStageComparator().getClass(),
                InputRateAndWaitingTimeComparator.class);
        assertEquals(((SharedThreadManager) ctx.getBean("ProtocolSchaedPool6")).getStageComparator().getClass(),
                ThreadCountAndWaitingTimeComparator.class);
        assertEquals(((SharedThreadManager) ctx.getBean("ProtocolSchaedPool7")).getStageComparator().getClass(),
                ThreadCountAndWaitingTimeComparator.class);
        assertEquals(((SharedThreadManager) ctx.getBean("ProtocolSchaedPool8")).getStageComparator().getClass(),
                TestComparator.class);
        assertEquals(stage.getThreadManager(), ctx.getBean("ProtocolSchaedPool8"));


        stage = manager.getStageInternal("TestStage21");
        assertEquals(((TestEventProcessor1) stage.getProcessor()).getNextStage().getName(), "TestStage20");


        assertTrue(manager.resolveObject("TestStage21") instanceof IStage);
        assertTrue(manager.resolveObject("CPAReqFlowBus") instanceof IFlowBus);
        assertTrue(manager.resolveObject("TestContext") instanceof IContextController);
        assertTrue(manager.resolveObject("TestTimer") instanceof ITimer);
        assertTrue(manager.resolveObject("testBean") instanceof TestObject);
        assertNull(manager.resolveObject("Not exists"));

        assertEquals("testBean", manager.resolveObjectName(manager.resolveObject("testBean")));
        assertNull(manager.resolveObjectName(new TestObject()));
    }
}
