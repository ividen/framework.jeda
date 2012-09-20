package ru.kwanza.jeda.core.pendingstore;

import ru.kwanza.autokey.api.AutoKeyValueSequence;
import ru.kwanza.autokey.api.IAutoKey;
import ru.kwanza.dbtool.UpdateException;
import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.ISink;
import ru.kwanza.jeda.api.ISuspender;
import ru.kwanza.jeda.api.SuspendException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static ru.kwanza.jeda.api.IPendingStore.SUSPEND_ID_ATTR;
import static ru.kwanza.jeda.api.IPendingStore.SUSPEND_SINK_NAME_ATTR;
import static ru.kwanza.jeda.api.Manager.resolveObjectName;

/**
 * @author Dmitry Zagorovsky
 */
public class Suspender<E extends IEvent> implements ISuspender<E> {

    private static final String SEQUENCE_NAME = Suspender.class.getName();

    private IAutoKey autoKey;
    private SuspenderDbInteraction dbInteraction;
    private String insertSql;

    private List<IEvent> suspends = new LinkedList<IEvent>();

    public Suspender(IAutoKey autoKey,
                     SuspenderDbInteraction dbInteraction,
                     String insertSql) {
        this.autoKey = autoKey;
        this.dbInteraction = dbInteraction;
        this.insertSql = insertSql;
    }

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
        try {
            dbInteraction.store(insertSql, suspends);
        } catch (UpdateException e) {
            throw new SuspendException(e.<IEvent>getConstrainted());
        }
    }

    protected Collection<E> suspendEvents(String sinkName, Collection<E> events) {
        AutoKeyValueSequence ids = getSuspendIdSequence(events.size());
        for (E event : events) {
            Long id = ids.next();
            SUSPEND_ID_ATTR.set(event, id);
            SUSPEND_SINK_NAME_ATTR.set(event, sinkName);
            suspends.add(event);
        }
        return events;
    }

    protected E suspendEvent(String sinkName, E event) {
        Long id = getNextSuspendInfoId();
        SUSPEND_ID_ATTR.set(event, id);
        SUSPEND_SINK_NAME_ATTR.set(event, sinkName);
        suspends.add(event);
        return event;
    }

    protected String getSinkName(ISink sink) {
        return resolveObjectName(sink);
    }

    protected Long getNextSuspendInfoId() {
        return autoKey.getNextValue(SEQUENCE_NAME);
    }

    protected AutoKeyValueSequence getSuspendIdSequence(int sequenceSize) {
        return autoKey.getValueSequence(SEQUENCE_NAME, sequenceSize);
    }

}
