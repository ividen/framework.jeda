package ru.kwanza.jeda.core.queue;

import com.atomikos.icatch.jta.AtomikosTransactionManager;
import ru.kwanza.jeda.api.*;
import ru.kwanza.jeda.api.internal.IStageInternal;
import ru.kwanza.jeda.api.internal.ISystemManager;
import ru.kwanza.jeda.api.internal.ITransactionManagerInternal;
import ru.kwanza.jeda.core.tm.BaseTransactionManager;
import junit.framework.TestSuite;

import javax.transaction.SystemException;

/**
 * @author Guzanov Alexander
 */
public class TestPriorityTransactionalMemoryQueueWithAtamicos extends TestPriorityTransactionalMemoryQueueWithStubTrx {
    private BaseTransactionManager tm;
    private AtomikosTransactionManager atamicos;


    private static class SystemManagerWithAtamikos implements ISystemManager {
        public ITransactionManagerInternal tm;

        public SystemManagerWithAtamikos(ITransactionManagerInternal tm) {
            this.tm = tm;
        }

        public ITransactionManagerInternal getTransactionManager() {
            return tm;
        }

        public IStage getStage(String name) {
            return null;
        }

        public IStageInternal getStageInternal(String name) {
            return null;
        }

        public ITimer getTimer(String name) {
            return null;
        }

        public IPendingStore getPendingStore() {
            throw new UnsupportedOperationException("getPendingStore");
        }

        public IContextController getContextController(String name) {
            return null;
        }

        public IFlowBus getFlowBus(String name) {
            return null;
        }

        public IStageInternal getCurrentStage() {
            return null;
        }

        public void setCurrentStage(IStageInternal stage) {
        }

        public IStage registerStage(IStageInternal stage) {
            return null;
        }

        public IFlowBus registerFlowBus(String name, IFlowBus flowBus) {
            return null;
        }

        public IContextController registerContextController(String name, IContextController context) {
            return null;
        }

        public ITimer registerTimer(String name, ITimer timer) {
            return null;
        }

        public void registerObject(String name, Object object) {
        }

        public String resolveObjectName(Object object) {
            return null;
        }

        public Object resolveObject(String objectName) {
            return null;
        }
    }

    public static TestSuite suite() {
        return new TestSuite(TestPriorityTransactionalMemoryQueueWithAtamicos.class);
    }

    public void setUp() throws Exception {
        atamicos = new AtomikosTransactionManager();
        atamicos.setForceShutdown(true);
        try {
            this.atamicos.init();
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }

        manager = new SystemManagerWithAtamikos(new BaseTransactionManager(atamicos));
    }

    public void tearDown() throws Exception {
    }
}
