package ru.kwanza.jeda.core.tm;

import ru.kwanza.jeda.api.internal.ITransactionManagerInternal;
import ru.kwanza.txn.impl.TransactionManagerImpl;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 * @author Guzanov Alexander
 */
public class BaseTransactionManager extends TransactionManagerImpl implements ITransactionManagerInternal {
    public BaseTransactionManager(TransactionManager jtaTransactionManager) {
        super(jtaTransactionManager);
    }

    public boolean hasTransaction() {
        return getTransaction() != null;
    }

    public Transaction getTransaction() {
        return stackThreadLocal.get().currentTransaction;
    }

    public void rollbackAllActive() {
        super.rollbackAllActive();
    }
}
