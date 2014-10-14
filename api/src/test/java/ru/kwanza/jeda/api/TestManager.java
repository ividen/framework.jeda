package ru.kwanza.jeda.api;

import ru.kwanza.jeda.api.internal.*;
import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Guzanov Alexander
 */
public class TestManager extends TestCase {

    public static class TestSystemManager implements ISystemManager {

        private IStageInternal stage;

        public ITransactionManagerInternal getTransactionManager() {
            throw new UnsupportedOperationException("getTransactionManager");
        }

        public IStage getStage(String name) {
            throw new UnsupportedOperationException("getStage_" + name);
        }

        public IStageInternal getStageInternal(String name) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public ITimer getTimer(String name) {
            throw new UnsupportedOperationException("getTimer_" + name);
        }

        public IPendingStore getPendingStore() {
            throw new UnsupportedOperationException("getPendingStore");
        }

        public IContextController getContextController(String name) {
            throw new UnsupportedOperationException("getContext_" + name);
        }

        public IFlowBus getFlowBus(String name) {
            throw new UnsupportedOperationException("getFlowBus_" + name);
        }

        public IStageInternal getCurrentStage() {
            return stage;
        }

        public void setCurrentStage(IStageInternal stage) {
            this.stage = stage;
        }

        public IStage registerStage(IStageInternal stage) {
            return null;
        }

        public IFlowBus registerFlowBus(String name, IFlowBus flowBus) {
            return null;
        }

        public IContextController registerContextController(String name, IContextController contextController) {
            return null;
        }

        public ITimer registerTimer(String name, ITimer timer) {
            return null;
        }

        public void registerObject(String name, Object object) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public String resolveObjectName(Object object) {
            return null;
        }

        public Object resolveObject(String objectName) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    public static final class TestStage implements IStageInternal {

        private String name;

        public TestStage(String name) {
            this.name = name;
        }

        public IThreadManager getThreadManager() {
            throw new UnsupportedOperationException("getThreadManager");
        }

        public IQueue getQueue() {
            throw new UnsupportedOperationException("getQueue");
        }

        public IAdmissionController getAdmissionController() {
            throw new UnsupportedOperationException("getAdmissionController");
        }

        public IFlowBus getFlowBus() {
            throw new UnsupportedOperationException("getFlowBus");
        }

        public IEventProcessor getProcessor() {
            throw new UnsupportedOperationException("getProcessor");
        }

        public boolean hasTransaction() {
            throw new UnsupportedOperationException("getProcessor");
        }

        public IResourceController getResourceController() {
            throw new UnsupportedOperationException("getResourceController");
        }

        public String getName() {
            return name;
        }

        public <E extends IEvent> ISink<E> getSink() {
            throw new UnsupportedOperationException("getSink");
        }
    }


    public void testManager() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-context.xml",
                TestManager.class);
        ISystemManagerInternal systemManager = (ISystemManagerInternal) ctx
                .getBean("ru.kwanza.jeda.api.ISystemManager");

        try {
            systemManager.getTransactionManager();
            fail("Excpected " + UnsupportedOperationException.class);
        } catch (UnsupportedOperationException e) {
            assertEquals("getTransactionManager", e.getMessage());
        }

        try {
            systemManager.getFlowBus("TetstFlowBus");
            fail("Excpected " + UnsupportedOperationException.class);
        } catch (UnsupportedOperationException e) {
            assertEquals("getFlowBus_TetstFlowBus", e.getMessage());
        }

        try {
            systemManager.getStage("TestStage");
            fail("Excpected " + UnsupportedOperationException.class);
        } catch (UnsupportedOperationException e) {
            assertEquals("getStage_TestStage", e.getMessage());
        }

        try {
            systemManager.getTimer("TestTimer");
            fail("Excpected " + UnsupportedOperationException.class);
        } catch (UnsupportedOperationException e) {
            assertEquals("getTimer_TestTimer", e.getMessage());
        }

        try {
            systemManager.<Object, IContext>getContextController("TestContext");
            fail("Excpected " + UnsupportedOperationException.class);
        } catch (UnsupportedOperationException e) {
            assertEquals("getContext_TestContext", e.getMessage());
        }


        TestStage testStage = new TestStage("TestStage");
        systemManager.setCurrentStage(testStage);

        assertEquals(systemManager.getCurrentStage(), testStage);

        try {
            systemManager.getFlowBus("TestContext");
            fail("Excpected " + UnsupportedOperationException.class);
        } catch (UnsupportedOperationException e) {
            assertEquals("getFlowBus_TestContext", e.getMessage());
        }

        systemManager.setCurrentStage(null);

    }

}
