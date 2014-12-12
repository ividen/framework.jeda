package ru.kwanza.jeda.core.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.api.internal.ITransactionManagerInternal;
import ru.kwanza.jeda.api.internal.SourceException;

import javax.transaction.Transaction;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Guzanov Alexander
 */
public abstract class AbstractTransactionalMemoryQueue<E extends IEvent> extends AbstractObservableMemoryQueue<E> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractTransactionalMemoryQueue.class);

    protected ConcurrentMap<Transaction, Tx> transactions = new ConcurrentHashMap<Transaction, Tx>();
    protected int maxSize;

    private IJedaManager manager;
    private ReentrantLock putLock = new ReentrantLock();
    private ReentrantLock takeLock = new ReentrantLock();
    private AtomicInteger size = new AtomicInteger(0);
    private AtomicInteger txPuts = new AtomicInteger(0);
    private AtomicInteger txTakes = new AtomicInteger(0);
    private ObjectCloneType objectCloneType;

    public AbstractTransactionalMemoryQueue(IJedaManager manager, ObjectCloneType objectCloneType, int maxSize) {
        this.objectCloneType = objectCloneType;
        this.maxSize = maxSize;
        this.manager = manager;
    }

    protected abstract void addToHead(E e);

    protected abstract void addToTail(E e);

    protected abstract void processTake(Tx tx, int c, ArrayList<E> result, ObjectOutputStreamEx oos);

    public int getEstimatedCount() {
        return size.get() - getTxTakesCount() + getTxPutsCount();
    }

    public void put(Collection<E> events) throws SinkException {
        if (events.isEmpty()) {
            return;
        }
        putLock();
        try {
            doPut(events);
        } finally {
            putUnlock();
        }
    }

    public Collection<E> tryPut(Collection<E> events) throws SinkException {
        if (events.isEmpty()) {
            return null;
        }
        putLock();
        try {
            return doTryPut(events);
        } finally {
            putUnlock();
        }
    }

    public int size() {
        return size.get();
    }

    public boolean isReady() {
        return (size() - txTakes.get()) > 0;
    }

    public Collection<E> take(int count) throws SourceException {
        if (count == 0) {
            return null;
        }
        takeLock();
        try {
            return doTake(count);
        } finally {
            takeUnlock();
        }
    }

    public int getMaxSize() {
        return maxSize;
    }

    protected void doPut(Collection<E> events) throws SinkException.Clogged {
        Tx tx = getCurrentTx();
        //todo aguzanov txPuts: volatile vs AtomicInteger
        int s = size.get() + getTxPutsCount();
        if (events.size() + s > maxSize) {
            throw new SinkException.Clogged("Sink maxSize=" + maxSize
                    + ",currentSize=" + s + ", try to put " + events.size() + " elements!");
        }
        if (tx == null) {
            for (E e : events) {
                addToTail(e);
            }
            int value = size.addAndGet(events.size());
            notify(value, events.size());
        } else {
            tx.logUndoPut(events);
            txPuts.addAndGet(events.size());
        }
    }

    protected Collection<E> doTake(int count) {
        //todo aguzanov txTakes: volatile vs AtomicInteger
        if (size() - getTxTakesCount() == 0) {
            return null;
        }
        Tx tx = getCurrentTx();

        int c = Math.min(size.get() - getTxTakesCount(), count);
        ArrayList<E> result = new ArrayList<E>(c);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStreamEx oos = null;
        try {
            oos = new ObjectOutputStreamEx(baos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        processTake(tx, c, result, oos);

        if (oos.getObjCount() > 0) {
            try {
                oos.flush();
            } catch (IOException e) {
                logger.error("Error flushing stream", e);
            }
            tx.logUndoTake(baos.toByteArray(), oos.getObjCount());

            try {
                oos.close();
            } catch (IOException e) {
                logger.error("Error closing stream", e);
            }
        }

        if (tx == null) {
            notify(size.get(), -result.size());
        }

        return result;
    }

    protected Collection<E> doTryPut(Collection<E> events) {
        Tx tx = getCurrentTx();
        MutableEventCollection result = new MutableEventCollection();
        if (tx == null) {
            int delta = 0;
            int value = 0;
            //todo aguzanov цилкы можно оптимизировать, учитывая, что
            for (E e : events) {
                //todo aguzanov review: в силу того, что мы находимся под putLock - size, может только уменьшится поэтому условие верное
                if (size() + getTxPutsCount() < maxSize) {
                    addToTail(e);
                    value = size.incrementAndGet();
                    delta++;
                } else {
                    result.add(e);
                }
            }
            notify(value, delta);
        } else {
            for (E e : events) {
                if (size.get() + getTxPutsCount() < maxSize) {
                    tx.logUndoPut(e);
                    txPuts.incrementAndGet();
                } else {
                    result.add(e);
                }
            }
        }

        if (result.size() == 0) return null;

        return result;
    }

    public Tx getCurrentTx() {
        Transaction transaction = getJTATransaction();
        if (transaction == null) {
            return null;
        }
        Tx tx = transactions.get(transaction);
        if (tx == null) {
            tx = new Tx(this, transaction);
            if (transactions.putIfAbsent(transaction, tx) != tx) {
                tx = transactions.get(transaction);
                try {
                    transaction.registerSynchronization(tx);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return tx;
    }

    protected int getTxPutsCount() {
        return txPuts.get();
    }

    protected int getTxTakesCount() {
        return txTakes.get();
    }

    protected Node iterateOverNodeAndTake(Node last, Tx tx, int c, ArrayList<E> result, ObjectOutputStreamEx oos) {
        while (result.size() < c && last.next != null) {
            last = last.next;
            E event = (E) last.event;
            result.add(event);
            if (tx != null) {
                try {
                    if (objectCloneType == ObjectCloneType.SERIALIZE) {
                        oos.writeObjectAndCount(event);
                    } else if (objectCloneType == ObjectCloneType.CLONE) {
                        tx.logUndoTake((E) event.getClass().getMethod("clone").invoke(event));
                    } else {
                        tx.logUndoTake(event);
                    }
                } catch (Exception e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Can't clone object! Use reference.", e);
                    }
                    tx.logUndoTake(event);
                }
                txTakes.incrementAndGet();
            } else {
                size.decrementAndGet();
            }
        }
        return last;
    }

    protected void putLock() throws SinkException.Closed {
        try {
            putLock.lockInterruptibly();
        } catch (InterruptedException e) {
            throw new SinkException.Closed(e);
        }
    }

    protected void putUnlock() {
        putLock.unlock();
    }

    protected void takeLock() throws SourceException {
        try {
            takeLock.lockInterruptibly();
        } catch (InterruptedException e) {
            throw new SourceException(e);
        }
    }

    protected void takeUnlock() {
        takeLock.unlock();
    }

    void commitPut(Collection<E> events) throws SinkException.Closed {
        int value;
        putLock();
        try {
            for (E e : events) {
                addToTail(e);
            }
            value = size.addAndGet(events.size());
            txPuts.addAndGet(-events.size());
            notify(value, events.size());
        } finally {
            putUnlock();
        }

    }

    void commitTake(int takeSize) throws SinkException.Closed {
        int value = size.addAndGet(-takeSize);
        txTakes.addAndGet(-takeSize);
        notify(value, -takeSize);
    }

    void rollbackPut(Collection<E> events) throws SinkException.Closed {
        txPuts.addAndGet(-events.size());
    }

    void rollbackTake(Collection undoTake, int undoSize) throws SinkException.Closed {
        try {
            takeLock.lockInterruptibly();
        } catch (InterruptedException e) {
            throw new SinkException.Closed(e);
        }
        try {
            if (objectCloneType == ObjectCloneType.SERIALIZE) {
                for (Object o : undoTake) {
                    if (o instanceof byte[]) {
                        try {
                            restoreFromBuffer((byte[]) o);
                        } catch (Exception ex) {
                            logger.error("Can't deserialize events in undo take!", ex);
                        }
                    } else {
                        addToHead((E) o);
                    }
                }
            } else {
                for (Object e : undoTake) {
                    addToHead((E) e);
                }
            }
            txTakes.addAndGet(-undoSize);
        } finally {
            takeLock.unlock();
        }
    }

    private Transaction getJTATransaction() {
        ITransactionManagerInternal tm = manager.getTransactionManager();
        return tm == null ? null : tm.getTransaction();
    }

    private void restoreFromBuffer(byte[] o) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) o);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object readObject;
        do {
            try {
                readObject = ois.readObject();
            } catch (EOFException e) {
                break;
            }
            addToHead((E) readObject);
        } while (readObject != null);
    }
}