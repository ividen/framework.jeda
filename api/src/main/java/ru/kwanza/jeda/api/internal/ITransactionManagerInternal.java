package ru.kwanza.jeda.api.internal;

import ru.kwanza.txn.api.spi.ITransactionManager;

import javax.transaction.Transaction;

/**
 * @author Guzanov Alexander
 */
public interface ITransactionManagerInternal extends ITransactionManager {
    public Transaction getTransaction();

    public void rollbackAllActive();
}
