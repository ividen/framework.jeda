package ru.kwanza.jeda.core.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.SinkException;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Guzanov Alexander
 */
public class Tx<E extends IEvent> implements Synchronization {
    private static final Logger logger = LoggerFactory.getLogger(TransactionalMemoryQueue.class);
    private Transaction jtaTx;
    private AbstractTransactionalMemoryQueue memoryQueue;
    private LinkedList putEvents = new LinkedList();
    private int undoTakeSize;
    private LinkedList undoTake = new LinkedList();
    private LinkedList<Callback> callbacks = new LinkedList<Callback>();

    Tx(AbstractTransactionalMemoryQueue memoryQueue, Transaction jtaTx) {
        this.memoryQueue = memoryQueue;
        this.jtaTx = jtaTx;
    }


    public interface Callback {
        public void beforeCompletion(boolean success);

        public void afterCompletion(boolean success);
    }

    public void beforeCompletion() {
    }

    public void afterCompletion(int i) {
        if (logger.isTraceEnabled()) {
            logger.trace("Tx for MemoryQueue status={}", i);
        }
        if (i == Status.STATUS_COMMITTED) {
            beforeCompletion(true);
            if (logger.isTraceEnabled()) {
                logger.trace("Commit MemoryQueue takeEventSize={}, putEventSize={}", undoTake.size(), putEvents.size());
            }
            if (!putEvents.isEmpty()) {
                try {
                    memoryQueue.commitPut(putEvents);
                } catch (SinkException.Closed e) {
                    logger.error("MemoryQueue closed!", e);
                }
            }

            if (!undoTake.isEmpty()) {
                try {
                    memoryQueue.commitTake(undoTakeSize);
                } catch (SinkException.Closed e) {
                    logger.error("MemoryQueue closed!", e);
                }
            }
            memoryQueue.transactions.remove(jtaTx);
            afterCompletion(true);
        }

        if (i == Status.STATUS_ROLLEDBACK) {
            if (logger.isTraceEnabled()) {
                logger.trace("Rollback MemoryQueue takeEventSize={}, putEventSize={}", undoTake.size(), putEvents.size());
            }

            beforeCompletion(false);
            if (!putEvents.isEmpty()) {
                try {
                    memoryQueue.rollbackPut(putEvents);
                } catch (SinkException.Closed e) {
                    logger.error("MemoryQueue closed!", e);
                }
            }

            if (!undoTake.isEmpty()) {
                try {
                    memoryQueue.rollbackTake(undoTake, undoTakeSize);
                } catch (SinkException.Closed e) {
                    logger.error("MemoryQueue closed!", e);
                }
            }
            memoryQueue.transactions.remove(jtaTx);
            afterCompletion(false);
        }
    }

    private void beforeCompletion(boolean success) {
        for (Callback c : callbacks) {
            try {
                c.beforeCompletion(success);
            } catch (Exception e) {
                logger.error("Error invoking callback!", e);
            }
        }
    }

    private void afterCompletion(boolean success) {
        for (Callback c : callbacks) {
            try {
                c.afterCompletion(success);
            } catch (Exception e) {
                logger.error("Error invoking callback!", e);
            }
        }
    }

    public void registerCallback(Callback callback) {
        callbacks.add(callback);
    }

    void logUndoPut(IEvent e) {
        putEvents.add(e);
    }

    void logUndoPut(Collection<E> events) {
        putEvents.addAll(events);
    }

    void logUndoTake(E e) {
        undoTake.add(e);
        undoTakeSize++;
    }

    void logUndoTake(byte[] events, int count) {
        undoTake.add(events);
        undoTakeSize += count;
    }
}
