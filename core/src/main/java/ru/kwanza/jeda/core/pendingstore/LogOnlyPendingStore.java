package ru.kwanza.jeda.core.pendingstore;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.IPendingStore;
import ru.kwanza.jeda.api.ISystemManager;
import ru.kwanza.jeda.api.ResumeException;

import java.util.Collection;

public class LogOnlyPendingStore implements IPendingStore {

    private ISystemManager manager;

    public LogOnlyPendingStore(ISystemManager manager) {
        this.manager = manager;
    }

    public <E extends IEvent> LogOnlySuspender<E> getSuspender() {
        return new LogOnlySuspender<E>(manager);
    }

    public void resume(Collection<Long> suspendItemsIds) throws ResumeException {
        throw new UnsupportedOperationException("It is non-persistent pending store.");
    }

    public void tryResume(Collection<Long> suspendItemsIds) throws ResumeException {
        throw new UnsupportedOperationException("It is non-persistent pending store.");
    }

    public void remove(Collection<Long> suspendItemsIds) {
        throw new UnsupportedOperationException("It is non-persistent pending store.");
    }

}
