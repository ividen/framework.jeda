package ru.kwanza.jeda.core.queue;

import ru.kwanza.jeda.api.*;
import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.api.internal.IStageInternal;
import ru.kwanza.jeda.api.internal.IJedaManagerInternal;
import ru.kwanza.jeda.api.internal.ITransactionManagerInternal;
import junit.framework.TestSuite;

/**
 * @author Guzanov Alexander
 */
public class TestPriorityTransactionalMemoryQueueWithoutTrx extends TestPriorityEventQueue {
    public static class NonTransactionalJedaManager implements IJedaManager {
        public ITransactionManagerInternal getTransactionManager() {
            return null;
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
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    public static TestSuite suite() {
        return new TestSuite(TestPriorityTransactionalMemoryQueueWithoutTrx.class);
    }

    public void testMaxSize() {
        PriorityTransactionalMemoryQueue memoryQueue = new PriorityTransactionalMemoryQueue(new NonTransactionalJedaManager());
        assertEquals("MaxSize wrong", Long.MAX_VALUE, memoryQueue.getMaxSize());
    }

    @Override
    protected IQueue createQueue() {
        return new PriorityTransactionalMemoryQueue(new NonTransactionalJedaManager(), ObjectCloneType.SERIALIZE, 10l);
    }
}