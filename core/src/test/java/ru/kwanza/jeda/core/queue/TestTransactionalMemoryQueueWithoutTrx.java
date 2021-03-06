package ru.kwanza.jeda.core.queue;

import junit.framework.Assert;
import org.junit.Test;
import org.springframework.transaction.PlatformTransactionManager;
import ru.kwanza.jeda.api.*;
import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.api.internal.IStageInternal;
import ru.kwanza.jeda.api.pushtimer.ITimer;

/**
 * @author Guzanov Alexander
 */
public class TestTransactionalMemoryQueueWithoutTrx extends TestEventQueue {
    public static class NonTransactionalJedaManager implements IJedaManager {
        public PlatformTransactionManager getTransactionManager() {
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
            return null;
        }
    }

    @Test
    public void testMaxSize() {
        TransactionalMemoryQueue memoryQueue = new TransactionalMemoryQueue(new NonTransactionalJedaManager());
        Assert.assertEquals("MaxSize wrong", Integer.MAX_VALUE, memoryQueue.getMaxSize());
    }

    @Override
    protected IQueue createQueue() {
        return new TransactionalMemoryQueue(new NonTransactionalJedaManager(), ObjectCloneType.SERIALIZE, 10);
    }
}
