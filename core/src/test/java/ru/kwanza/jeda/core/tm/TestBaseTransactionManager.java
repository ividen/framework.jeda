package ru.kwanza.jeda.core.tm;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.kwanza.jeda.api.*;
import ru.kwanza.jeda.api.internal.IStageInternal;
import ru.kwanza.jeda.api.internal.ITransactionManagerInternal;
import ru.kwanza.jeda.api.pushtimer.ITimer;
import ru.kwanza.jeda.core.queue.Event;
import ru.kwanza.jeda.core.queue.ObjectCloneType;
import ru.kwanza.jeda.core.queue.TransactionalMemoryQueue;
import junit.framework.TestCase;
import ru.kwanza.txn.impl.TransactionException;

import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import java.util.Collections;

/**
 * @author Guzanov Alexander
 */
public abstract class TestBaseTransactionManager extends TestCase {
    protected TransactionManager jtaTM;
    protected BaseTransactionManager tm;
    protected ClassPathXmlApplicationContext ctx;

    public void setUp() throws Exception {
        ctx = new ClassPathXmlApplicationContext(getContextPath(), TestBaseTransactionManager.class);
        tm = (BaseTransactionManager) ctx.getBean("baseTransactionManager");
        jtaTM = (TransactionManager) ctx.getBean("jtaTransactionManager");
    }

    protected abstract String getContextPath();

    @Override
    public void tearDown() throws Exception {
        ctx.close();
    }

    public void testBeginBeginCommitCommit() throws SystemException {
        tm.begin();
        assertNotNull("We must have transaction!", jtaTM.getTransaction());
        assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
        {
            tm.begin();
            assertNotNull("We must have transaction!", jtaTM.getTransaction());
            assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
            tm.commit();
            assertNotNull("We must have transaction!", jtaTM.getTransaction());
            assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
        }
        tm.commit();
        assertNull("Transaction must be finished!", jtaTM.getTransaction());
        assertNull("Transaction must be finished!", tm.getTransaction());
    }

    //
    public void testBeginBeginRollbackCommit() throws SystemException {
        tm.begin();
        assertNotNull("We must have transaction!", jtaTM.getTransaction());
        assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
        {
            tm.begin();
            assertNotNull("We must have transaction!", jtaTM.getTransaction());
            assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
            tm.rollback();
            assertNotNull("We must have transaction!", jtaTM.getTransaction());
            assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
        }
        tm.commit();
        assertNull("Transaction must be finished!", jtaTM.getTransaction());
        assertNull("Transaction must be finished!", tm.getTransaction());
    }


    public void testBeginBeginRollbackRollback() throws SystemException {
        tm.begin();
        assertNotNull("We must have transaction!", jtaTM.getTransaction());
        assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
        {
            tm.begin();
            assertNotNull("We must have transaction!", jtaTM.getTransaction());
            assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
            tm.rollback();
            assertNotNull("We must have transaction!", jtaTM.getTransaction());
            assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
        }
        tm.commit();
        assertNull("Transaction must be finished!", jtaTM.getTransaction());
        assertNull("Transaction must be finished!", tm.getTransaction());
    }

    public void testBeginCommit() throws SystemException {
        tm.begin();
        assertNotNull("We must have transaction!", jtaTM.getTransaction());
        assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
        tm.commit();
        assertNull("Transaction must be finished!", jtaTM.getTransaction());
        assertNull("Transaction must be finished!", tm.getTransaction());
    }

    //
    public void testBeginRollback() throws SystemException {
        tm.begin();
        assertNotNull("We must have transaction!", jtaTM.getTransaction());
        assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
        tm.rollback();
        assertNull("Transaction must be finished!", jtaTM.getTransaction());
        assertNull("Transaction must be finished!", tm.getTransaction());
    }

    public void testCheckJTA() throws SystemException {
        try {
            tm.setJtaTransactionManager(null);
            fail("Must be Exception");
        } catch (RuntimeException e) {
        }
    }

    public void testCheckStatusOnBegin() throws SystemException {
        tm.setJtaTransactionManager(jtaTM);
        tm.begin();
        assertNotNull("We must have transaction!", jtaTM.getTransaction());
        assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
        jtaTM.setRollbackOnly();

        try {
            tm.commit();
            fail("Must be TransactionException");
        } catch (TransactionException e) {
        }
    }

    //    public void testRollback0Active() throws SystemException {
//        BaseTransactionManager tm = new BaseTransactionManager(jtaTM);
//        assertEquals("Tx count", 0, tm.getTxCount());
//        assertNull("No txs", tm.getTransaction());
//        tm.rollbackAllActive();
//        assertEquals("Tx count", 0, tm.getTxCount());
//        assertNull("No txs", tm.getTransaction());
//    }
//
    public void testRollbackActive50() throws SinkException, SystemException {

        TransactionalMemoryQueue queue = new TransactionalMemoryQueue(new IJedaManager() {
            public ITransactionManagerInternal getTransactionManager() {
                return tm;
            }

            public IStage getStage(String name) {
                return null;
            }

            public IStageInternal getStageInternal(String name) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
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
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public String resolveObjectName(Object object) {
                return null;
            }

            public Object resolveObject(String objectName) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        }, ObjectCloneType.SERIALIZE, 100);

        for (int i = 0; i < 50; i++) {
            tm.begin();
            assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
            queue.put(Collections.<IEvent>singleton(new Event(String.valueOf(i))));
            assertEquals("QueueSize ", 0, queue.size());
        }


        tm.rollbackAllActive();
        assertEquals("QueueSize ", 0, queue.size());
    }

    public void testSetJtaTransactionManager() throws SystemException {
        tm.setJtaTransactionManager(jtaTM);
        tm.begin();
        assertNotNull("We must have transaction!", jtaTM.getTransaction());
        assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
        tm.commit();

        assertNull("We must have transaction!", jtaTM.getTransaction());
        assertNull("We must have transaction!", tm.getTransaction());
    }

    public void testSetRollback() throws SystemException {
        tm.begin();
        assertNotNull("We must have transaction!", jtaTM.getTransaction());
        assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
        jtaTM.setRollbackOnly();
        try {
            tm.commit();
            fail("Must be RuntimeException ");
        } catch (Throwable e) {
            assertEquals("Must be RuntimeException", e.getClass(), TransactionException.class);
        }
        tm.rollbackAllActive();
    }

    public void testWithMemoryQuueue() throws SinkException, SystemException {

        TransactionalMemoryQueue queue = new TransactionalMemoryQueue(new IJedaManager() {
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
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public String resolveObjectName(Object object) {
                return null;
            }

            public Object resolveObject(String objectName) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        }, ObjectCloneType.SERIALIZE, 100);

        for (int i = 0; i < 50; i++) {
            tm.begin();
            assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
            queue.put(Collections.<IEvent>singleton(new Event(String.valueOf(i))));
            assertEquals("QueueSize ", 0, queue.size());
        }

        for (int i = 0; i < 50; i++) {
            assertEquals("QueueSize ", i, queue.size());
            tm.commit();
            assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
        }

        assertEquals("QueueSize ", 50, queue.size());
    }

    public void testBeginBeginSuspendResumeCommitCommit() throws SystemException {
        tm.begin();
        assertNotNull("We must have transaction!", jtaTM.getTransaction());
        assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
        {
            tm.begin();
            assertNotNull("We must have transaction!", jtaTM.getTransaction());
            assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
            {
                tm.suspend();
                assertNull("We must have transaction!", jtaTM.getTransaction());
                assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
                tm.resume();
            }
            tm.commit();
            assertNotNull("We must have transaction!", jtaTM.getTransaction());
            assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
        }
        tm.commit();
        assertNull("Transaction must be finished!", jtaTM.getTransaction());
        assertNull("Transaction must be finished!", tm.getTransaction());
    }

    public void testBeginBeginSuspendBeginCommitResumeCommitCommit() throws SystemException {
        tm.begin();
        assertNotNull("We must have transaction!", jtaTM.getTransaction());
        assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
        {
            tm.begin();
            assertNotNull("We must have transaction!", jtaTM.getTransaction());
            assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
            {
                tm.suspend();
                assertNull("We must have transaction!", jtaTM.getTransaction());
                assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
                {
                    tm.begin();
                    assertNotNull("We must have transaction!", jtaTM.getTransaction());
                    assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
                    tm.commit();
                }
                tm.resume();
            }
            tm.commit();
            assertNotNull("We must have transaction!", jtaTM.getTransaction());
            assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
        }
        tm.commit();
        assertNull("Transaction must be finished!", jtaTM.getTransaction());
        assertNull("Transaction must be finished!", tm.getTransaction());
    }

    public void testBeginBeginSuspendBeginCommitResumeCommitCommit_WithRollbackAll() throws SystemException {
        try {
            tm.begin();
            assertNotNull("We must have transaction!", jtaTM.getTransaction());
            assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
            {
                tm.begin();
                assertNotNull("We must have transaction!", jtaTM.getTransaction());
                assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
                {
                    tm.suspend();
                    assertNull("We must have transaction!", jtaTM.getTransaction());
                    assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
                    {
                        tm.begin();
                        assertNotNull("We must have transaction!", jtaTM.getTransaction());
                        assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
                        if (true) {
                            throw new RuntimeException();
                        }
                        tm.commit();
                    }
                    tm.resume();
                }
                tm.commit();
                assertNotNull("We must have transaction!", jtaTM.getTransaction());
                assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
            }
            tm.commit();
            assertNull("Transaction must be finished!", jtaTM.getTransaction());
            assertNull("Transaction must be finished!", tm.getTransaction());
        } catch (Exception e) {
            tm.rollbackAllActive();
        }

        assertNull("Transaction must be finished!", jtaTM.getTransaction());
        assertNull("Transaction must be finished!", tm.getTransaction());
    }

    public void testBeginResumeCommit() throws SystemException {
        tm.begin();
        assertNotNull("We must have transaction!", jtaTM.getTransaction());
        assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
        try {
            tm.resume();
            fail("Expected " + TransactionException.class);
        } catch (TransactionException e) {
        }
        tm.commit();
        assertNull("Transaction must be finished!", jtaTM.getTransaction());
        assertNull("Transaction must be finished!", tm.getTransaction());
    }

    public void testBeginBeginResumeCommitCommit() throws SystemException {
        tm.begin();
        assertNotNull("We must have transaction!", jtaTM.getTransaction());
        assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
        {
            tm.begin();
            assertNotNull("We must have transaction!", jtaTM.getTransaction());
            assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
            {
                try {
                    tm.resume();
                    fail("Expected " + TransactionException.class);
                } catch (TransactionException e) {
                }
            }
            tm.commit();
            assertNotNull("We must have transaction!", jtaTM.getTransaction());
            assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
        }
        tm.commit();
        assertNull("Transaction must be finished!", jtaTM.getTransaction());
        assertNull("Transaction must be finished!", tm.getTransaction());
    }

    public void testSuspendResume() throws SystemException {
        tm.suspend();
        assertNull("We must have transaction!", jtaTM.getTransaction());
        assertEquals("We must have transaction!", jtaTM.getTransaction(), tm.getTransaction());
        tm.resume();
        assertNull("Transaction must be finished!", jtaTM.getTransaction());
        assertNull("Transaction must be finished!", tm.getTransaction());
    }

}
