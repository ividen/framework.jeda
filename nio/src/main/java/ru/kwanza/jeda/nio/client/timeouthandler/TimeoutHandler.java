package ru.kwanza.jeda.nio.client.timeouthandler;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.attributes.Attribute;
import ru.kwanza.jeda.nio.client.AbstractFilter;

import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Alexander Guzanov
 */
public class TimeoutHandler extends Thread {
    private static volatile TimeoutHandler instance;
    private static ReentrantLock lock = new ReentrantLock();
    private ConcurrentSkipListSet<Connection> connetions = new ConcurrentSkipListSet<Connection>(new ConnectionComparator());
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
        if (connetions.remove(connection)) {
//            checkTimedOut(connection);
        }
        TIMEOUT.remove(connection);
    }

    private void registerWrite0(long ts, Connection connection) {
        if (TIMEOUT.get(connection) == null) {
            TIMEOUT.set(connection, new Record(ts));
            connetions.add(connection);
        } else {
            TIMEOUT.get(connection).timestamp.set(System.currentTimeMillis() + ts);
            if(connetions.remove(connection)){
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
                final Iterator<Connection> i = connetions.iterator();

                while (i.hasNext()) {
                    final Connection connection = i.next();
                    final Record record = TIMEOUT.get(connection);
//                    if (record != null && record.timestamp.get() < System.currentTimeMillis()) {
//                        if (connetions.remove(connection)) {
//                            record.throwReadError.set(true);
//                            connection.closeSilently();
//                        }
//                    } else {
//                        break;
//                    }
                }

                synchronized (this) {
                    try {
                        wait(1000L);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            } catch (Exception e) {

            }
        }
    }

    private static final TimeoutHandler getInstance() {
        if (instance == null) {
            lock.lock();
            if (instance == null) {
                try {
                    instance = new TimeoutHandler();
                    instance.start();
                } finally {
                    lock.unlock();
                }
            }
        }

        return instance;
    }

}
