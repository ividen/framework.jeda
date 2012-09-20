package ru.kwanza.jeda.core.queue;

import com.arjuna.ats.internal.jta.transaction.arjunacore.JBossTransactionManager;
import ru.kwanza.jeda.api.*;
import ru.kwanza.jeda.api.internal.IStageInternal;
import ru.kwanza.jeda.api.internal.ISystemManager;
import ru.kwanza.jeda.api.internal.ITransactionManagerInternal;
import ru.kwanza.jeda.core.tm.BaseTransactionManager;
import junit.framework.TestSuite;

import javax.transaction.TransactionManager;

/**
 * @author Guzanov Alexander
 */
public class TestPriorityTransactionalMemoryQueueWithArjuna extends TestPriorityTransactionalMemoryQueueWithStubTrx {
    private BaseTransactionManager tm;
    private TransactionManager arjuna;


    private static class SystemManagerWithArjuna implements ISystemManager {
        public ITransactionManagerInternal tm;

        public SystemManagerWithArjuna(ITransactionManagerInternal tm) {
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
        return new TestSuite(TestPriorityTransactionalMemoryQueueWithArjuna.class);
    }

    public void setUp() throws Exception {
        arjuna = new JBossTransactionManager(true);
        manager = new SystemManagerWithArjuna(new BaseTransactionManager(arjuna));
    }

    public void tearDown() throws Exception {
    }
}

