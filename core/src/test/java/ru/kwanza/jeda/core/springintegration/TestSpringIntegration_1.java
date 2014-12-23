package ru.kwanza.jeda.core.springintegration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.internal.IJedaManagerInternal;
import ru.kwanza.jeda.api.internal.IResourceController;
import ru.kwanza.jeda.api.internal.IStageInternal;
import ru.kwanza.jeda.core.resourcecontroller.FixedBatchSizeResourceController;
import ru.kwanza.jeda.core.resourcecontroller.SmartResourceController;
import ru.kwanza.jeda.core.resourcecontroller.StaticResourceController;
import ru.kwanza.jeda.core.threadmanager.stage.StageThreadManager;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

/**
 * @author Guzanov Alexander
 */


@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration("application-config.xml")
public class TestSpringIntegration_1 {

    @Autowired
    private ApplicationContext ctx;

    @Test
    public void test1() {
        IJedaManagerInternal manager = (IJedaManagerInternal) ctx.getBean(IJedaManager.class);

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
}
