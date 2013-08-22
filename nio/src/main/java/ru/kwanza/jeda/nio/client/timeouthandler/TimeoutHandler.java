package ru.kwanza.jeda.nio.client.timeouthandler;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.attributes.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kwanza.jeda.nio.client.AbstractFilter;

import java.net.SocketTimeoutException;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Alexander Guzanov
 */
public class TimeoutHandler extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(TimeoutHandler.class);

    private static volatile TimeoutHandler instance;
    private static ReentrantLock lock = new ReentrantLock();
    private PriorityBlockingQueue<Connection> connetions = new PriorityBlockingQueue<Connection>(100, new ConnectionComparator());
    private AtomicLong w = new AtomicLong();
    static Attribute<Record> TIMEOUT = Grizzly.DEFAULT_ATTRIBUTE_BUILDER.createAttribute(AbstractFilter.class.getSimpleName() + ".TIMEOUT");

    TimeoutHandler() {
        super(AbstractFilter.class.getSimpleName() + "-ReadTimeoutWorker");
        setDaemon(true);
    }

    public static void registerRead(Connection connection) throws SocketTimeoutException {
        getInstance().registerRead0(connection);
    }

    public static void registerWrite(long ts, Connection connection) {
        getInstance().registerWrite0(ts, connection);
    }

    public static void checkTimedOut(Connection connection) throws SocketTimeoutException {
        getInstance().checkTimedOut0(connection);
    }

    private void registerRead0(Connection connection) throws SocketTimeoutException {
        connetions.remove(connection);
        TIMEOUT.remove(connection);
    }

    private void forget0(Connection connection) {
        connetions.remove(connection);
        TIMEOUT.remove(connection);
    }

    private void registerWrite0(long ts, Connection connection) {
        if (TIMEOUT.get(connection) == null) {
            TIMEOUT.set(connection, new Record(ts));
            connetions.add(connection);
        } else {
            TIMEOUT.get(connection).timestamp.set(System.currentTimeMillis() + ts);
            if (connetions.remove(connection)) {
                connetions.add(connection);
            }

        }

        w.incrementAndGet();
    }

    public void checkTimedOut0(Connection connection) throws SocketTimeoutException {
        final Record record = TIMEOUT.get(connection);
        try {
            if (record != null && record.throwReadError.get()) {
                throw new SocketTimeoutException("Timed out occured! Cant't read response!");
            }
        } finally {
            if (record != null) {
                connetions.remove(connection);
            }
        }
    }

    @Override
    public final void run() {
        while (isAlive() && !isInterrupted()) {
            try {
                Connection connection;
                while ((connection = connetions.peek()) != null) {
                    if (connection != null) {
                        final Record record = TIMEOUT.get(connection);
                        if (record != null && record.timestamp.get() < System.currentTimeMillis()) {
                            if (connetions.remove(connection)) {
                                record.throwReadError.set(true);
                                connection.closeSilently();
                            }
                        } else {
                            break;
                        }
                    }
                }
                synchronized (this) {
                    try {
                        wait(1000L);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            } catch (Exception e) {
                logger.error("Unexpected exception occured!", e);
            }
        }

    }

    private static final TimeoutHandler getInstance() {
        if (instance == null) {
            lock.lock();
            try {
                if (instance == null) {
                    instance = new TimeoutHandler();
                    instance.start();
                }
            } finally {
                lock.unlock();
            }
        }

        return instance;
    }

    public static void forget(Connection connection) {
        getInstance().forget0(connection);
    }
}
