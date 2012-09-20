package ru.kwanza.jeda.mock;

import ru.kwanza.jeda.api.internal.ITransactionManagerInternal;
import ru.kwanza.txn.mock.MockTransactionManager;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Guzanov Alexander
 */
public class MockTransactionManagerInternal implements ITransactionManagerInternal, org.springframework.context.ApplicationContextAware {
    private static final MockTransactionManagerInternal instance = new MockTransactionManagerInternal();

    private Stack<MockTx> txs = new Stack<MockTx>();
    private MockTx currentTx;
    private final AtomicLong txCounter = new AtomicLong(0l);
    private ApplicationContext applicationContext;

    public synchronized void clear() {
        currentTx = null;
        txs.clear();
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public final class MockTx {
        private ArrayList<MockTxSync> syncs = new ArrayList<MockTxSync>();
        private TransactionStatus status;
        private DefaultTransactionDefinition def;

        public MockTx() {
        }

        private boolean hasDataSource() {
            try {
                return applicationContext.getBean("dataSource") != null;
            } catch (BeansException e) {

            }
            return false;
        }

        public ArrayList<MockTxSync> getSyncs() {
            return syncs;
        }

        public void commit() {
            if (MockTransactionManager.getInstance().hasTransaction()) {
                MockTransactionManager.getInstance().commit();
            }

            for (MockTxSync s : syncs) {
                s.commit();
            }
        }

        public void rollback() {
            if (MockTransactionManager.getInstance().hasTransaction()) {
                MockTransactionManager.getInstance().rollback();
            }

            for (MockTxSync s : syncs) {
                s.rollback();
            }
        }
    }

    public Transaction getTransaction() {
        return null;
    }

    public void suspend() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void resume() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void registerSynchronization(Synchronization synchronization) {
        throw new UnsupportedOperationException();
    }

    public void registerSynchronization(int index, Synchronization synchronization) {
        throw new UnsupportedOperationException();
    }

    public synchronized void rollbackAllActive() {
        while (hasTransaction()) {
            rollback();
        }
    }

    public synchronized void begin() {
        MockTx mockTx = new MockTx();
        MockTransactionManager.getInstance().begin();

        if (currentTx != null) {
            txs.push(currentTx);
        }
        currentTx = mockTx;

    }

    public synchronized void commit() {
        currentTx.commit();
        currentTx = null;
        if (!txs.isEmpty()) {
            currentTx = txs.pop();
        }
    }

    public synchronized void rollback() {
        currentTx.rollback();
        currentTx = null;
        if (!txs.isEmpty()) {
            currentTx = txs.pop();
        }
    }

    public synchronized Stack<MockTx> getTxs() {
        return txs;
    }

    public MockTx getCurrentTx() {
        return currentTx;
    }

    public synchronized boolean hasTransaction() {
        return currentTx != null;
    }

    public static MockTransactionManagerInternal getInstance() {
        return instance;
    }

}
