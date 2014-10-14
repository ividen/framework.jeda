package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.*;
import ru.kwanza.jeda.api.internal.IResourceController;
import ru.kwanza.jeda.api.internal.IStageInternal;
import ru.kwanza.jeda.api.ISystemManager;
import ru.kwanza.jeda.api.internal.ISystemManagerInternal;
import ru.kwanza.jeda.core.manager.SystemContextController;
import ru.kwanza.jeda.core.manager.SystemFlowBus;
import ru.kwanza.jeda.core.manager.SystemTimer;
import ru.kwanza.jeda.core.resourcecontroller.FixedBatchSizeResourceController;
import ru.kwanza.jeda.core.resourcecontroller.SmartResourceController;
import ru.kwanza.jeda.core.resourcecontroller.StaticResourceController;
import ru.kwanza.jeda.core.threadmanager.shared.SharedThreadManager;
import ru.kwanza.jeda.core.threadmanager.shared.comparator.*;
import ru.kwanza.jeda.core.threadmanager.stage.StageThreadManager;
import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Guzanov Alexander
 */
public class TestSpringIntegration extends TestCase {

    public void test1() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-config.xml", TestSpringIntegration.class);
        ISystemManagerInternal manager = (ISystemManagerInternal) ctx.getBean(ISystemManager.class);

        IStageInternal stage = manager.getStageInternal("TestStage1");
        assertEquals(stage.getName(), "TestStage1");
        assertEquals(stage.getResourceController().getClass(), StaticResourceController.class);
        assertEquals(stage.getAdmissionController().getClass(), TestAdmissionController.class);
        assertEquals(stage.getProcessor().getClass(), TestEventProcessor.class);
        assertEquals(stage.getThreadManager().getClass(), StageThreadManager.class);

        stage = manager.getStageInternal("TestStage2");
        assertEquals(stage.getName(), "TestStage2");
        assertEquals(stage.getResourceController().getClass(), StaticResourceController.class);
        assertNull(stage.getAdmissionController());
        assertEquals(stage.getProcessor().getClass(), TestEventProcessor.class);
        assertEquals(stage.getThreadManager().getClass(), StageThreadManager.class);


        stage = manager.getStageInternal("TestStage3");
        assertEquals(stage.getName(), "TestStage3");
        assertEquals(stage.getResourceController().getClass(), StaticResourceController.class);
        assertNull(stage.getAdmissionController());
        assertEquals(stage.getProcessor().getClass(), TestEventProcessor.class);
        assertEquals(stage.getThreadManager().getClass(), StageThreadManager.class);


        stage = manager.getStageInternal("TestStage4");
        assertEquals(stage.getName(), "TestStage4");
        IResourceController resourceController = stage.getResourceController();
        assertEquals(resourceController.getClass(), SmartResourceController.class);
        assertEquals(((SmartResourceController) resourceController).getMaxBatchSize(), 10000);
        assertEquals(((SmartResourceController) resourceController).getMaxThreadCount(), Runtime.getRuntime().availableProcessors());
        assertNull(stage.getAdmissionController());
        assertEquals(stage.getProcessor().getClass(), TestEventProcessor.class);
        assertEquals(stage.getThreadManager().getClass(), StageThreadManager.class);


        stage = manager.getStageInternal("TestStage5");
        assertEquals(stage.getName(), "TestStage5");
        resourceController = stage.getResourceController();
        assertEquals(resourceController.getClass(), SmartResourceController.class);
        assertEquals(((SmartResourceController) resourceController).getMaxBatchSize(), 10000);
        assertEquals(((SmartResourceController) resourceController).getMaxThreadCount(), Runtime.getRuntime().availableProcessors());
        assertEquals(((SmartResourceController) resourceController).getBatchSize(), 1);
        assertNull(stage.getAdmissionController());
        assertEquals(stage.getProcessor().getClass(), TestEventProcessor.class);
        assertEquals(stage.getThreadManager().getClass(), StageThreadManager.class);

        stage = manager.getStageInternal("TestStage6");
        assertEquals(stage.getName(), "TestStage6");
        resourceController = stage.getResourceController();
        assertEquals(resourceController.getClass(), SmartResourceController.class);
        assertEquals(((SmartResourceController) resourceController).getMaxBatchSize(), 500);
        assertEquals(((SmartResourceController) resourceController).getMaxThreadCount(), 10);
        assertEquals(((SmartResourceController) resourceController).getBatchSize(), 1);
        assertEquals(((SmartResourceController) resourceController).getProcessingTimeThreshold(), 8000);
        assertNull(stage.getAdmissionController());
        assertEquals(stage.getProcessor().getClass(), TestEventProcessor.class);
        assertEquals(stage.getThreadManager().getClass(), StageThreadManager.class);


        stage = manager.getStageInternal("TestStage7");
        assertEquals(stage.getName(), "TestStage7");
        resourceController = stage.getResourceController();
        assertEquals(resourceController.getClass(), SmartResourceController.class);
        assertEquals(((SmartResourceController) resourceController).getMaxBatchSize(), 10000);
        assertEquals(((SmartResourceController) resourceController).getMaxThreadCount(), Runtime.getRuntime().availableProcessors());
        assertEquals(((SmartResourceController) resourceController).getBatchSize(), 10);
        assertEquals(((SmartResourceController) resourceController).getProcessingTimeThreshold(), 60000);
        assertNull(stage.getAdmissionController());
        assertEquals(stage.getProcessor().getClass(), TestEventProcessor.class);
        assertEquals(stage.getThreadManager().getClass(), StageThreadManager.class);

        stage = manager.getStageInternal("TestStage8");
        assertEquals(stage.getName(), "TestStage8");
        resourceController = stage.getResourceController();
        assertEquals(resourceController.getClass(), SmartResourceController.class);
        assertEquals(((SmartResourceController) resourceController).getMaxBatchSize(), 10000);
        assertEquals(((SmartResourceController) resourceController).getMaxThreadCount(), Runtime.getRuntime().availableProcessors());
        assertEquals(((SmartResourceController) resourceController).getBatchSize(), 1);
        assertEquals(((SmartResourceController) resourceController).getProcessingTimeThreshold(), 60000);
        assertNull(stage.getAdmissionController());
        assertEquals(stage.getProcessor().getClass(), TestEventProcessor.class);
        assertEquals(stage.getThreadManager().getClass(), StageThreadManager.class);


        stage = manager.getStageInternal("TestStage9");
        assertEquals(stage.getName(), "TestStage9");
        resourceController = stage.getResourceController();
        assertEquals(resourceController.getClass(), SmartResourceController.class);
        assertEquals(((SmartResourceController) resourceController).getMaxBatchSize(), 500);
        assertEquals(((SmartResourceController) resourceController).getMaxThreadCount(), 10);
        assertEquals(((SmartResourceController) resourceController).getBatchSize(), 1);
        assertEquals(((SmartResourceController) resourceController).getProcessingTimeThreshold(), 9000);
        assertNull(stage.getAdmissionController());
        assertEquals(stage.getProcessor().getClass(), TestEventProcessor.class);
        assertEquals(stage.getThreadManager().getClass(), StageThreadManager.class);

        stage = manager.getStageInternal("TestStage10");
        assertEquals(stage.getName(), "TestStage10");
        assertEquals(stage.getResourceController().getClass(), StaticResourceController.class);
        assertNull(stage.getAdmissionController());
        assertEquals(stage.getProcessor().getClass(), TestEventProcessor.class);
        assertEquals(stage.getThreadManager().getClass(), StageThreadManager.class);

        stage = manager.getStageInternal("TestStage11");
        assertEquals(stage.getName(), "TestStage11");
        assertEquals(stage.getResourceController().getClass(), StaticResourceController.class);
        assertNull(stage.getAdmissionController());
        assertEquals(stage.getProcessor().getClass(), TestEventProcessor.class);
        assertEquals(stage.getThreadManager().getClass(), StageThreadManager.class);


        stage = manager.getStageInternal("TestStage12");
        assertEquals(stage.getName(), "TestStage12");
        assertEquals(stage.getResourceController().getClass(), StaticResourceController.class);
        assertNull(stage.getAdmissionController());
        assertEquals(stage.getProcessor().getClass(), TestEventProcessor.class);
        assertEquals(stage.getThreadManager().getClass(), StageThreadManager.class);

        stage = manager.getStageInternal("TestStage13");
        assertEquals(stage.getName(), "TestStage13");
        assertEquals(stage.getResourceController().getClass(), StaticResourceController.class);
        assertNull(stage.getAdmissionController());
        assertEquals(stage.getProcessor().getClass(), TestEventProcessor.class);
        assertEquals(stage.getThreadManager().getClass(), StageThreadManager.class);

        stage = manager.getStageInternal("TestStage14");
        assertEquals(stage.getName(), "TestStage14");
        assertEquals(stage.getResourceController().getClass(), StaticResourceController.class);
        assertNull(stage.getAdmissionController());
        assertEquals(stage.getProcessor().getClass(), TestEventProcessor.class);
        assertEquals(stage.getThreadManager().getClass(), StageThreadManager.class);


        stage = manager.getStageInternal("TestStage15");
        assertEquals(stage.getName(), "TestStage15");
        assertEquals(stage.getResourceController().getClass(), ResourceController1.class);
        assertNull(stage.getAdmissionController());
        assertEquals(stage.getProcessor().getClass(), TestEventProcessor.class);
        assertEquals(stage.getThreadManager().getClass(), StageThreadManager.class);

        stage = manager.getStageInternal("TestStage16");
        assertEquals(stage.getName(), "TestStage16");
        assertEquals(stage.getResourceController().getClass(), ResourceController2.class);
        assertNull(stage.getAdmissionController());
        assertEquals(stage.getProcessor().getClass(), TestEventProcessor.class);
        assertEquals(stage.getThreadManager().getClass(), StageThreadManager.class);

        stage = manager.getStageInternal("TestStage17");
        assertEquals(stage.getName(), "TestStage17");
        assertEquals(stage.getResourceController().getClass(), FixedBatchSizeResourceController.class);
        assertNull(stage.getAdmissionController());
        assertEquals(stage.getProcessor().getClass(), TestEventProcessor.class);
        assertEquals(stage.getThreadManager().getClass(), StageThreadManager.class);

        stage = manager.getStageInternal("TestStage18");
        assertEquals(stage.getName(), "TestStage18");
        assertEquals(stage.getResourceController().getClass(), FixedBatchSizeResourceController.class);
        assertNull(stage.getAdmissionController());
        assertEquals(stage.getProcessor().getClass(), TestEventProcessor.class);
        assertEquals(stage.getThreadManager().getClass(), StageThreadManager.class);
    }

    public void test2() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-config_1.xml", TestSpringIntegration.class);
        ISystemManagerInternal manager = (ISystemManagerInternal) ctx.getBean(ISystemManager.class.getName());

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
