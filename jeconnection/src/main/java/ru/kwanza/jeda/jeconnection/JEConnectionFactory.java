package ru.kwanza.jeda.jeconnection;

import com.sleepycat.je.Durability;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.TransactionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kwanza.filelock.FileLockHelper;
import ru.kwanza.jeda.api.IJedaManager;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Guzanov Alexander
 */
public class JEConnectionFactory {
    static final Logger logger = LoggerFactory.getLogger(JEConnectionFactory.class);

    private final ConcurrentMap<Integer, JEConnection> connections =
            new ConcurrentHashMap<Integer, JEConnection>();

    private EnvironmentConfig environmentConfig = new EnvironmentConfig()
            .setAllowCreate(true).setTransactional(true)
            .setTxnTimeout(1, TimeUnit.MINUTES).setLockTimeout(2, TimeUnit.MINUTES);

    protected IJedaManager manager;

    private ReentrantLock lock = new ReentrantLock();
    private String path;
    private TransactionConfig transactionConfig = new TransactionConfig()
            .setDurability(Durability.COMMIT_SYNC);

    private long lockingTimeout = 30 * 1000;
    private volatile boolean active = true;


    public JEConnectionFactory(IJedaManager manager) {
        this.manager = manager;
    }

    public EnvironmentConfig getEnvironmentConfig() {
        return environmentConfig;
    }

    public void setEnvironmentConfig(EnvironmentConfig environmentConfig) {
        this.environmentConfig = environmentConfig;
    }

    public long getLockingTimeout() {
        return lockingTimeout;
    }

    public void setLockingTimeout(long lockingTimeout) {
        this.lockingTimeout = lockingTimeout;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public TransactionConfig getTransactionConfig() {
        return transactionConfig;
    }

    public void setTransactionConfig(TransactionConfig transactionConfig) {
        this.transactionConfig = transactionConfig;
    }

    public void closeConnection(Integer nodeId) {
        JEConnection connection = connections.get(nodeId);
        if (connection != null) {
            lock.lock();
            try {
                connections.remove(nodeId);
                connection.close();
            } finally {
                lock.unlock();
            }
        }
    }

    public JEConnection getConnection(Integer nodeId) {
        if (!active) {
            throw new JEConnectionException("Factory destroyed!");
        }
        JEConnection connection = findConnection(nodeId);
        connection.enlist();
        return connection;
    }

    public JEConnection getTxConnection(Integer nodeId) {
        if (!active) {
            throw new JEConnectionException("Factory destroyed!");
        }
        JEConnection connection = findConnection(nodeId);
        connection.enlistTx();
        return connection;
    }

    private JEConnection findConnection(Integer nodeId) {
        JEConnection connection = connections.get(nodeId);
        if (connection == null) {
            lock.lock();
            try {
                connection = connections.get(nodeId);
                if (connection == null) {
                    String filePath = path + File.separatorChar + nodeId;
                    File envFileDir = new File(filePath);
                    if (!envFileDir.exists()) {
                        if (!envFileDir.mkdirs() && !envFileDir.exists()) {
                            logger.error("Can't create enviroment dir {}", filePath);
                            throw new JEConnectionException("Can't create dirs for Berkeley environment!");
                        }
                    }

                    String fileLockPath = filePath + File.separatorChar + "fileLock.lock";
                    File file = new File(fileLockPath);
                    if (!file.exists()) {
                        try {
                            if (!file.createNewFile() && !file.exists()) {
                                throw new IOException();
                            }
                        } catch (IOException e) {
                            logger.error("Can't create file lock {}", fileLockPath);
                            throw new JEConnectionException("Can't create file lock for Berkeley environment!", e);
                        }
                    }


                    Lock fileLock = FileLockHelper.getFileLock(file.getAbsolutePath());

                    try {
                        if (!fileLock.tryLock(lockingTimeout, TimeUnit.MILLISECONDS)) {
                            throw new JEConnectionException("Can't lock file!");
                        }
                    } catch (InterruptedException e) {
                        throw new JEConnectionException("Can't lock file!", e);
                    }


                    JEEnvironment environment = new JEEnvironment(envFileDir, environmentConfig);
                    connection = new JEConnection(this, nodeId, fileLock, environment);
                    connections.put(nodeId, connection);
                }
            } finally {
                lock.unlock();
            }
        }
        return connection;
    }

    public void destroy() {
        logger.info("Destroying connection factory");
        if (!active) {
            return;
        }
        active = false;
        HashSet<Integer> nodeIds = new HashSet<Integer>(this.connections.keySet());
        for (Integer nodeId : nodeIds) {
            closeConnection(nodeId);
        }
    }
}
