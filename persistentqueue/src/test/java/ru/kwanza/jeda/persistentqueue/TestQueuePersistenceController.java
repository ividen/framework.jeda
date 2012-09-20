package ru.kwanza.jeda.persistentqueue;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public final class TestQueuePersistenceController implements IQueuePersistenceController {

    private boolean errorONLoad = false;
    private boolean errorONTransfer = false;
    private boolean returnNullONTransfer = false;
    private boolean errorOnPersist = false;


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
        TestDataStore.getInstance().delete(nodeId, result);
    }

    public Collection<EventWithKey> load(long nodeId) {
        if (errorONLoad) {
            throw new RuntimeException("Error On Load");
        }
        TestDataStore dataStore = TestDataStore.getInstance();
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
        TestDataStore.getInstance().persist(nodeId, events);
    }

    public Collection<EventWithKey> transfer(long count, long currentNodeId, long newNodeId) {
        if (errorONTransfer) {
            throw new RuntimeException("Error on transfer");
        }
        if (returnNullONTransfer) {
            return null;
        }
        TestDataStore dataStore = TestDataStore.getInstance();
        dataStore.lock();
        try {
            return dataStore.transfer(count, currentNodeId, newNodeId);
        } finally {
            dataStore.unlock();
        }
    }
}
