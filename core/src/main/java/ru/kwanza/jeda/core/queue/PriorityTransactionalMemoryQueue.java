package ru.kwanza.jeda.core.queue;

import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.IPriorityEvent;
import ru.kwanza.jeda.api.SinkException;

import java.util.*;

/**
 * @author Guzanov Alexander
 */
public class PriorityTransactionalMemoryQueue<E extends IPriorityEvent> extends AbstractTransactionalMemoryQueue<E> {
    private Map<IPriorityEvent.Priority, Node> heads = new HashMap<IPriorityEvent.Priority, Node>();
    private Map<IPriorityEvent.Priority, Node> tails = new HashMap<IPriorityEvent.Priority, Node>();

    public PriorityTransactionalMemoryQueue(IJedaManager manager) {
        this(manager, ObjectCloneType.SERIALIZE, Integer.MAX_VALUE);
    }

    public PriorityTransactionalMemoryQueue(IJedaManager manager, ObjectCloneType objectCloneType, int maxSize) {
        super(manager, objectCloneType, maxSize);
        for (IPriorityEvent.Priority p : IPriorityEvent.Priority.values()) {
            Node lastNode;
            Node firstNode = lastNode = new Node(null);
            heads.put(p, firstNode);
            tails.put(p, lastNode);
        }
    }

    public Collection<E> tryPut(Collection<E> events) throws SinkException {
        if (events.isEmpty()) {
            return null;
        }

        ArrayList<E> sortedEvents = new ArrayList<E>(events);
        Collections.sort(sortedEvents, PriorityEventComparator.INSTANCE);
        return super.tryPut(sortedEvents);
    }

    @Override
    protected void addToHead(E e) {
        Node firstNode = heads.get(e.getPriority());
        firstNode.event = e;
        heads.put(e.getPriority(), new Node<E>(null, firstNode));
    }

    protected void addToTail(E e) {
        Node lastNode = tails.get(e.getPriority());
        lastNode = lastNode.next = new Node(e);
        tails.put(e.getPriority(), lastNode);
    }

    protected void processTake(Tx tx, int c, ArrayList<E> result, ObjectOutputStreamEx oos) {
        for (IPriorityEvent.Priority p : IPriorityEvent.Priority.values()) {
            Node<E> first = heads.get(p);
            Node<E> last = first;
            last = iterateOverNodeAndTake(last, tx, c, result, oos);

            if (first != last) {
                heads.put(p, last);
                last.event = null;
            }

            if (result.size() >= c) {
                break;
            }
        }
    }
}