package ru.kwanza.jeda.persistentqueue.old;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public final class TestQueuePersistenceController implements IQueuePersistenceController {

    private boolean errorONLoad = false;
    private boolean errorONTransfer = false;
    private boolean returnNullONTransfer = false;
    private boolean errorOnPersist = false;
    private TestDataStore dataStore;

    public TestQueuePersistenceController(TestDataStore dataStore) {
        this.dataStore = dataStore;
    }

    public void setErrorONLoad(boolean errorONLoad) {
        this.errorONLoad = errorONLoad;
    }

    public void setErrorONTransfer(boolean errorONTransfer) {
        this.errorONTransfer = errorONTransfer;
    }

    public void setReturnNullONTransfer(boolean returnNullONTransfer) {
        this.returnNullONTransfer = returnNullONTransfer;
    }

    public void setErrorOnPersist(boolean errorOnPersist) {
        this.errorOnPersist = errorOnPersist;
    }

    public void delete(Collection<EventWithKey> result, long nodeId) {
        dataStore.delete(nodeId, result);
    }

    public Collection<EventWithKey> load(long nodeId) {
        if (errorONLoad) {
            throw new RuntimeException("Error On Load");
        }
        dataStore.lock();
        try {
            return dataStore.getEventsWithKey(nodeId);
        } finally {
            dataStore.unlock();
        }
    }

    public void persist(Collection<EventWithKey> events, long nodeId) {
        if (errorOnPersist) {
            throw new RuntimeException("Error on persist!");
        }
        dataStore.persist(nodeId, events);
    }

    public Collection<EventWithKey> transfer(int count, long currentNodeId, long newNodeId) {
        if (errorONTransfer) {
            throw new RuntimeException("Error on transfer");
        }
        if (returnNullONTransfer) {
            return null;
        }
        dataStore.lock();
        try {
            return dataStore.transfer(count, currentNodeId, newNodeId);
        } finally {
            dataStore.unlock();
        }
    }
}
