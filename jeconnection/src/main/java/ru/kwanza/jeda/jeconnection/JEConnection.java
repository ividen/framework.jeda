package ru.kwanza.jeda.jeconnection;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import ru.kwanza.jeda.api.internal.ITransactionManagerInternal;

import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static ru.kwanza.jeda.jeconnection.JEConnectionFactory.logger;

/**
 * @author Guzanov Alexander
 */
public class JEConnection {
    private Lock fileLock;
    private JEEnvironment environment;
    private ReentrantLock lock = new ReentrantLock();
    private JEConnectionFactory factory;
    private ConcurrentHashMap<Transaction, Transaction> enlistment = new ConcurrentHashMap<Transaction, Transaction>();
    private ConcurrentHashMap<String, Database> databases = new ConcurrentHashMap<String, Database>();
    private Integer nodeId;

    JEConnection(JEConnectionFactory factory, Integer nodeId, Lock fileLock, JEEnvironment environment) {
        this.fileLock = fileLock;
        this.environment = environment;
        this.factory = factory;
        this.nodeId = nodeId;
    }

    JEEnvironment getEnvironment() {
        return environment;
    }

    public Database openDatabase(String name, DatabaseConfig config) {
        Database database = databases.get(name);
        if (database == null) {
            lock.lock();
            try {
                if ((database = databases.get(name)) == null) {
                    ITransactionManagerInternal transactionManager = factory.manager.getTransactionManager();
                    transactionManager.suspend();

                    try {
                        database = environment.openDatabase(null, name, config);
                        databases.put(name, database);
                    } finally {
                        transactionManager.resume();
                    }
                }
            } finally {
                lock.unlock();
            }
        }
        return database;
    }

    protected void enlist() {
        ITransactionManagerInternal transactionManager = factory.manager.getTransactionManager();
        if (transactionManager.hasTransaction()) {
            Transaction transaction = transactionManager.getTransaction();
            if (!enlistment.contains(transaction)) {
                if (null == enlistment.putIfAbsent(transaction, transaction)) {
                    try {
                        transaction.enlistResource(this.getEnvironment());
                        transaction.registerSynchronization(new CleanupAfterCompletion(transaction));
                    } catch (Exception e) {
                        enlistment.remove(transaction);
                        throw new JEConnectionException(e);
                    }
                }
            }
        }
    }

    protected void enlistTx() {
        ITransactionManagerInternal transactionManager = factory.manager.getTransactionManager();
        if (!transactionManager.hasTransaction()) {
            throw new JEConnectionException("Expected active transaction!");
        }


        Transaction transaction = transactionManager.getTransaction();
        if (!enlistment.contains(transaction)) {
            if (null == enlistment.putIfAbsent(transaction, transaction)) {
                try {
                    transaction.enlistResource(this.getEnvironment());
                    transaction.registerSynchronization(new CleanupAndCloseAfterComplete(transaction));
                } catch (Exception e) {
                    throw new JEConnectionException(e);
                }
            }
        }

    }

    private class CleanupAfterCompletion implements Synchronization {
        private Transaction transaction;

        private CleanupAfterCompletion(Transaction transaction) {
            this.transaction = transaction;
        }

        public void beforeCompletion() {
        }

        public void afterCompletion(int status) {
            enlistment.remove(transaction);
        }
    }

    private class CleanupAndCloseAfterComplete implements Synchronization {
        private Transaction transaction;

        private CleanupAndCloseAfterComplete(Transaction transaction) {
            this.transaction = transaction;
        }


        public void beforeCompletion() {
        }

        public void afterCompletion(int status) {
            enlistment.remove(transaction);
            factory.closeConnection(nodeId);
        }
    }

    void close() {
        lock.lock();
        try {
            String path = getEnvironment().getHome().getAbsolutePath();
            logger.info("Closing environment {}", path);
            Set<Transaction> transactions = new HashSet<Transaction>(enlistment.keySet());
            for (Transaction transaction : transactions) {
                logger.warn("Marking transaction {} for rollback for connection {}", transaction,
                        this.getEnvironment().getHome().getAbsolutePath());

                try {
                    transaction.rollback();
                } catch (Throwable e) {
//                    e.printStackTrace();
                }

            }

            for (Database db : databases.values()) {
                db.close();
            }
            databases.clear();
            try {
                getEnvironment().close();
            } catch (Exception e) {
                logger.warn("Error closing environment " + path, e);
            }
        } finally {
            fileLock.unlock();
            lock.unlock();
        }
    }
}
