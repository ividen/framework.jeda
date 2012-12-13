package ru.kwanza.jeda.core.tm;

import ru.kwanza.jeda.api.internal.ITransactionManagerInternal;
import ru.kwanza.txn.api.spi.ITransactionManager;
import ru.kwanza.txn.impl.TransactionManagerImpl;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 * @author Guzanov Alexander
 */
public class BaseTransactionManager implements ITransactionManagerInternal {
    private TransactionManagerImpl tm;

    public BaseTransactionManager(TransactionManagerImpl tm) {
        this.tm = tm;
    }

    public void setJtaTransactionManager(TransactionManager tm) {
        this.tm.setJtaTransactionManager(tm);
    }

    public void resume() {
        tm.resume();
    }

    public void suspend() {
        tm.suspend();
    }

    public void rollback() {
        tm.rollback();
    }

    public void commit() {
        tm.commit();
    }

    public void begin() {
        tm.begin();
    }

    public Transaction getTransaction() {
        return tm.getTransaction();
    }

    public void rollbackAllActive() {
        tm.rollbackAllActive();
    }

    public boolean hasTransaction() {
        return tm.hasTransaction();
    }
}
