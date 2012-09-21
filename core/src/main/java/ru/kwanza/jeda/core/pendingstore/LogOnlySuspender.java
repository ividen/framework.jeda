package ru.kwanza.jeda.core.pendingstore;

import ru.kwanza.jeda.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static ru.kwanza.jeda.api.IPendingStore.SUSPEND_ID_ATTR;
import static ru.kwanza.jeda.api.IPendingStore.SUSPEND_SINK_NAME_ATTR;
import static ru.kwanza.jeda.api.Manager.resolveObjectName;

public class LogOnlySuspender<E extends IEvent> implements ISuspender<E> {

    private static final Logger log = LoggerFactory.getLogger(LogOnlySuspender.class);

    private List<E> suspends = new LinkedList<E>();
    private static AtomicLong counter = new AtomicLong(0l);

    public E suspend(ISink<E> sink, E event) {
        return suspendEvent(getSinkName(sink), event);
    }

    public E suspend(String sinkName, E event) {
        return suspendEvent(sinkName, event);
    }

    public Collection<E> suspend(ISink<E> sink, Collection<E> events) {
        return suspendEvents(getSinkName(sink), events);
    }

    public Collection<E> suspend(String sinkName, Collection<E> events) {
        return suspendEvents(sinkName, events);
    }

    public void flush() throws SuspendException {
        for (IEvent event : suspends) {
            if (log.isWarnEnabled()) {
                log.warn("Suspend event with non-persistent suspender {" +
                        "id={} , sinkName={}, event={}", new Object[]{SUSPEND_ID_ATTR.get(event),
                        SUSPEND_SINK_NAME_ATTR.get(event), event});
            }
        }
    }

    protected String getSinkName(ISink sink) {
        return resolveObjectName(sink);
    }

    protected Collection<E> suspendEvents(String sinkName, Collection<E> events) {
        Collection<E> suspendedItemList = new LinkedList<E>();
        for (E event : events) {
            suspendedItemList.add(suspendEvent(sinkName, event));
        }
        return suspendedItemList;
    }

    protected E suspendEvent(String sinkName, E event) {
        SUSPEND_ID_ATTR.set(event, counter.incrementAndGet());
        SUSPEND_SINK_NAME_ATTR.set(event, sinkName);
        suspends.add(event);
        return event;
    }

    //For tests
    List<E> getSuspends() {
        return suspends;
    }


}
