package ru.kwanza.jeda.jeconnection;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.*;

import javax.transaction.SystemException;
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

    public Database openDatabase(final String name, final DatabaseConfig config) {
        Database database = databases.get(name);
        if (database == null) {
            lock.lock();
            try {
                if ((database = databases.get(name)) == null) {
                    final PlatformTransactionManager transactionManager = factory.manager.getTransactionManager();
                    TransactionTemplate template = new TransactionTemplate(transactionManager,
                            new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NOT_SUPPORTED));

                    database = template.execute(new TransactionCallback<Database>() {
                        @Override
                        public Database doInTransaction(TransactionStatus status) {
                            return environment.openDatabase(null, name, config);
                        }

                    });
                    databases.put(name, database);
                }
            } finally {
                lock.unlock();
            }
        }
        return database;
    }

    protected void enlist() {
        final JtaTransactionManager tm = (JtaTransactionManager) factory.manager.getTransactionManager();
        if (TransactionSynchronizationManager.isActualTransactionActive()) {

            final Transaction transaction;
            try {
                transaction = tm.getTransactionManager().getTransaction();
            } catch (SystemException e) {
                throw new JEConnectionException(e);
            }

            if (!enlistment.contains(transaction)) {
                if (null == enlistment.putIfAbsent(transaction, transaction)) {
                    try {
                        transaction.enlistResource(this.getEnvironment());
                        TransactionSynchronizationManager.registerSynchronization(new CleanupAfterCompletion());
                    } catch (Exception e) {
                        enlistment.remove(transaction);
                    }
                }
            }
        }

    }

    protected void enlistTx() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new JEConnectionException("Expected active transaction!");
        }

        final JtaTransactionManager tm = (JtaTransactionManager) factory.manager.getTransactionManager();
        final Transaction transaction;
        try {
            transaction = tm.getTransactionManager().getTransaction();
        } catch (SystemException e) {
            throw new JEConnectionException(e);
        }
        if (!enlistment.contains(transaction)) {
            if (null == enlistment.putIfAbsent(transaction, transaction)) {
                try {
                    transaction.enlistResource(this.getEnvironment());
                    TransactionSynchronizationManager.registerSynchronization(new CleanupAndCloseAfterComplete(transaction));
                } catch (Exception e) {
                    throw new JEConnectionException(e);
                }
            }
        }

    }

    class CleanupAfterCompletion extends TransactionSynchronizationAdapter {
        @Override
        public void suspend() {
            TransactionSynchronizationManager.unbindResourceIfPossible(environment);
        }

        @Override
        public void resume() {
            TransactionSynchronizationManager.bindResource(environment, environment);
        }

        @Override
        public void beforeCompletion() {
            TransactionSynchronizationManager.unbindResourceIfPossible(environment);
        }
    }

    private class CleanupAndCloseAfterComplete extends TransactionSynchronizationAdapter {
        private Transaction transaction;

        public CleanupAndCloseAfterComplete(Transaction transaction) {
            this.transaction = transaction;
        }

        @Override
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
