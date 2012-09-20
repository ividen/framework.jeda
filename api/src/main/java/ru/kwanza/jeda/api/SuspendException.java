package ru.kwanza.jeda.api;

import java.util.List;

/**
 * @author Dmitry Zagorovsky
 */
public class SuspendException extends RuntimeException {

    private List<IEvent> failedToSuspendEvents;

    public SuspendException(List<IEvent> failedToSuspendEvents) {
        this.failedToSuspendEvents = failedToSuspendEvents;
    }

    @SuppressWarnings("unchecked")
    public <E extends IEvent> List<E> getFailedToSuspendEvents() {
        return (List<E>) failedToSuspendEvents;
    }

}
