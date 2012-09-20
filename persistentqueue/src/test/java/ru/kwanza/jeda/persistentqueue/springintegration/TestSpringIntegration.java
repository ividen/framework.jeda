package ru.kwanza.jeda.persistentqueue.springintegration;

import ru.kwanza.jeda.api.internal.IStageInternal;
import ru.kwanza.jeda.api.internal.ISystemManager;
import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Guzanov Alexander
 */
public class TestSpringIntegration extends TestCase {

    public void test() throws InterruptedException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-config.xml", TestSpringIntegration.class);
        ISystemManager manager = (ISystemManager) ctx.getBean(ISystemManager.class.getName());

        IStageInternal stage = manager.getStageInternal("TestStage31");

        assertEquals(stage.getName(), "TestStage31");

        stage = manager.getStageInternal("TestStage31");
        assertEquals(stage.getName(), "TestStage31");

        stage = manager.getStageInternal("TestStage32");
        assertEquals(stage.getName(), "TestStage32");

        stage = manager.getStageInternal("TestStage33");
        assertEquals(stage.getName(), "TestStage33");

    }

}
