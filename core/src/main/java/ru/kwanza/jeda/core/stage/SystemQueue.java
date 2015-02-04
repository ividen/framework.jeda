package ru.kwanza.jeda.core.stage;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.ISink;
import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.api.internal.IAdmissionController;
import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.api.internal.IQueueObserver;
import ru.kwanza.jeda.api.internal.SourceException;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class SystemQueue<E extends IEvent> implements IQueue<E> {
    private IQueue<E> queue;
    private IAdmissionController<E> admissionController;
    private boolean isWrapped = false;
    private SystemSink systemSink;
    private Stage stage;


    public class SystemSink implements ISink<E> {

        public void put(Collection<E> events) throws SinkException {
            SystemQueue.this.put(events);
        }

        public Collection<E> tryPut(Collection<E> events) throws SinkException {
            return SystemQueue.this.tryPut(events);
        }

        public String getName() {
            return SystemQueue.this.stage.getName();
        }
    }

    public SystemQueue(IQueue<E> queue, IAdmissionController<E> admissionController, Stage stage) {
        this.queue = queue;
        this.stage = stage;
        this.queue.setObserver(stage);
        this.admissionController = admissionController;
        this.systemSink = new SystemSink();
    }

    public void setObserver(IQueueObserver observer) {
        throw new UnsupportedOperationException("SystemQueue not support observable!");
    }

    ISink<E> asSink() {
        return systemSink;
    }

    public IQueueObserver getObserver() {
        return queue.getObserver();
    }

    public int getEstimatedCount() {
        return queue.getEstimatedCount();
    }

    public boolean isReady() {
        return queue.isReady();
    }

    public void put(Collection<E> events) throws SinkException {
        if (admissionController != null) {
            admissionController.accept(events);
        }
        try {
            queue.put(events);
        } catch (SinkException e) {
            if (admissionController != null) {
                admissionController.degrade(events);
            }
            throw e;
        }
    }

    public Collection<E> tryPut(Collection<E> events) throws SinkException {
        ArrayList<E> list = new ArrayList<E>(events);
        Collection<E> admissionResult = null;
        Collection<E> queueResult = null;
        if (admissionController != null) {
            admissionResult = admissionController.tryAccept(list);
            list.removeAll(admissionResult);
        }
        try {
            queueResult = queue.tryPut(list);
        } catch (SinkException e) {
            if (admissionController != null) {
                admissionController.degrade(events);
            }

            throw e;
        }

        if (admissionResult == null && queueResult == null) {
            return null;
        }

        ArrayList<E> result = new ArrayList<E>(calculateSize(admissionResult, queueResult));
        if (admissionResult != null) {
            result.addAll(admissionResult);
        }

        if (queueResult != null) {
            if (admissionController != null) {
                admissionController.degrade(queueResult);
            }
            result.addAll(queueResult);
        }


        return result;
    }

    public Collection<E> take(int count) throws SourceException {
        return queue.take(count);
    }

    public int size() {
        return queue.size();
    }

    private int calculateSize(Collection<E> c1, Collection<E> c2) {
        return (c1 == null ? 0 : c1.size()) + (c2 == null ? 0 : c2.size());
    }
}
