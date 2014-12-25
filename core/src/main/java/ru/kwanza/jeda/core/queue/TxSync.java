package ru.kwanza.jeda.core.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.SinkException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Guzanov Alexander
 */
public class TxSync<E extends IEvent> implements TransactionSynchronization {
    private static final Logger logger = LoggerFactory.getLogger(TransactionalMemoryQueue.class);
    private AbstractTransactionalMemoryQueue<E> memoryQueue;
    private List putEvents = new ArrayList();
    private int undoTakeSize;
    private List undoTake = new ArrayList();

    TxSync(AbstractTransactionalMemoryQueue<E> memoryQueue) {
        this.memoryQueue = memoryQueue;
    }


    public static <E extends IEvent> TxSync<E> getTxSync(AbstractTransactionalMemoryQueue<E> memoryQueue) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TxSync<E> result = (TxSync<E>) TransactionSynchronizationManager.getResource(memoryQueue);
            if (result == null) {
                result = new TxSync<E>(memoryQueue);
                TransactionSynchronizationManager.bindResource(memoryQueue,result);
                TransactionSynchronizationManager.registerSynchronization(result);
            }
            return result;
        } else {
            return null;
        }
    }

    @Override
    public void suspend() {
        TransactionSynchronizationManager.unbindResourceIfPossible(memoryQueue);
    }

    @Override
    public void resume() {
        TransactionSynchronizationManager.bindResource(memoryQueue,this);
    }

    @Override
    public void flush() {
    }

    @Override
    public void beforeCommit(boolean readOnly) {
    }

    public void beforeCompletion() {
        TransactionSynchronizationManager.unbindResourceIfPossible(memoryQueue);
    }

    @Override
    public void afterCommit() {
    }

    public void afterCompletion(int i) {
        if (logger.isTraceEnabled()) {
            logger.trace("Tx for MemoryQueue status={}", i);
        }
        if (i == TransactionSynchronization.STATUS_COMMITTED) {
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
        }

        if (i == TransactionSynchronization.STATUS_ROLLED_BACK) {
            if (logger.isTraceEnabled()) {
                logger.trace("Rollback MemoryQueue takeEventSize={}, putEventSize={}", undoTake.size(), putEvents.size());
            }

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
        }
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
