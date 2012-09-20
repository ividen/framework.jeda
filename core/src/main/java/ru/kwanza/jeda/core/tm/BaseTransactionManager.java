package ru.kwanza.jeda.core.tm;

import ru.kwanza.jeda.api.internal.ITransactionManagerInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 * @author Guzanov Alexander
 */
public class BaseTransactionManager implements ITransactionManagerInternal {
    private static final Logger logger = LoggerFactory.getLogger(BaseTransactionManager.class);
    public TransactionManager tm;

    private ThreadLocal<TransactionStack> stackThreadLocal = new ThreadLocal<TransactionStack>() {
        protected TransactionStack initialValue() {
            return new TransactionStack();
        }
    };

    public BaseTransactionManager() {
    }

    public BaseTransactionManager(TransactionManager tm) {
        chekJtaTm(tm);
        this.tm = tm;
    }

    public boolean hasTransaction() {
        return getTransaction() != null;
    }

    public void begin() {
        chekJtaTm(tm);
        try {
            TransactionStack transactionStack = stackThreadLocal.get();
            Transaction currentTx = tm.suspend();
            if (currentTx != null) {
                transactionStack.push(currentTx);
            }
            tm.begin();
            transactionStack.currentTransaction = tm.getTransaction();
        } catch (Exception e) {
            logError(e);
            throw new TransactionException(e);
        }
    }

    public void commit() {
        chekJtaTm(tm);
        Transaction next = null;
        TransactionStack transactionStack = stackThreadLocal.get();
        try {
            next = transactionStack.getNext();
            transactionStack.currentTransaction.commit();
        } catch (TransactionException e) {
            throw e;
        } catch (Exception e) {
            logError(e);
            throw new TransactionException(e);
        } finally {
            transactionStack.currentTransaction = next;
            if (transactionStack.currentTransaction != null) {
                try {
                    tm.resume(transactionStack.currentTransaction);
                } catch (Exception e) {
                    logError(e);
                }
            }
        }
    }

    public void rollback() {
        chekJtaTm(tm);
        TransactionStack transactionStack = stackThreadLocal.get();
        try {
            transactionStack.currentTransaction.rollback();
        } catch (Exception e) {
            logError(e);
        } finally {
            transactionStack.currentTransaction = transactionStack.getNext();
            if (transactionStack.currentTransaction != null) {
                try {
                    tm.resume(transactionStack.currentTransaction);
                } catch (Exception e) {
                    throw new TransactionException(e);
                }
            }
        }
    }

    public Transaction getTransaction() {
        chekJtaTm(tm);
        return stackThreadLocal.get().currentTransaction;
    }

    public void suspend() {
        chekJtaTm(tm);
        TransactionStack transactionStack = stackThreadLocal.get();
        try {
            if (tm.getStatus() == Status.STATUS_MARKED_ROLLBACK || (tm.getStatus() == Status.STATUS_ROLLING_BACK)) {
                throw new TransactionException("Current transaction marked for rolling back," +
                        " you can't begin new subtransaction!");
            }
            Transaction currentTx = tm.suspend();
            if (currentTx != null) {
                transactionStack.push(new SuspendMarker(currentTx));
            }
            transactionStack.currentTransaction = null;
        } catch (Exception e) {
            logError(e);
            throw new TransactionException(e);
        }
    }

    public void resume() {
        chekJtaTm(tm);
        Object next = null;
        if (hasTransaction()) {
            throw new TransactionException("We are not under suspend section!");
        }
        TransactionStack transactionStack = stackThreadLocal.get();
        try {
            next = transactionStack.isEmpty() ? null : transactionStack.peek();
            if (next != null) {
                if (next instanceof SuspendMarker) {
                    try {
                        transactionStack.pop();
                        transactionStack.currentTransaction = ((SuspendMarker) next).tx;
                        tm.resume(transactionStack.currentTransaction);
                    } catch (Exception e) {
                        logError(e);
                    }
                } else {
                    throw new TransactionException("Try to resume transaction " +
                            "that is not under suspend section!");
                }
            }
        } catch (TransactionException e) {
            throw e;
        } catch (Exception e) {
            logError(e);
            throw new TransactionException(e);
        }
    }

    public void rollbackAllActive() {
        chekJtaTm(tm);
        TransactionStack transactionStack = stackThreadLocal.get();
        if (logger.isDebugEnabled()) {
            logger.debug("Rolling back all active tx in scope of {}", Thread.currentThread().getName());
        }
        if (transactionStack.currentTransaction == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No txs in scope of {}", Thread.currentThread().getName());
            }
            return;
        }

        while (transactionStack.currentTransaction != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Rolling back tx {}" + transactionStack.currentTransaction);
            }
            try {
//                if (tm.getTransaction() != transactionStack.currentTransaction) {
//                    throw new TransactionException("Transaction stack is inconsistent! " +
//                            "Current jta transaction differ from current FlexFlow trx!");
//                }
//                tm.rollback();
                transactionStack.currentTransaction.rollback();
            } catch (SystemException e) {
                logError(e);
            }

            Object o = transactionStack.isEmpty() ? null : transactionStack.pop();
            transactionStack.currentTransaction = (o instanceof SuspendMarker ?
                    ((SuspendMarker) o).tx : (Transaction) o);
            if (transactionStack.currentTransaction != null) {
                try {
                    tm.resume(transactionStack.currentTransaction);
                } catch (Exception e) {
                    logError(e);
                }
            }
        }
    }

    public int getTxCount() {
        return stackThreadLocal.get().size() + (getTransaction() != null ? 1 : 0);
    }

    public void setJtaTransactionManager(TransactionManager tm) {
        chekJtaTm(tm);
        this.tm = tm;
    }

    private void chekJtaTm(TransactionManager tm) {
        if (tm == null) {
            throw new RuntimeException("We need jta-compliant transaction manager!");
        }
    }

    private void logError(Exception e) {
        logger.error("Error in TM", e);
    }
}
