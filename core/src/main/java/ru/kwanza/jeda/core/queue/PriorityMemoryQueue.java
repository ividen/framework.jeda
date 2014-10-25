package ru.kwanza.jeda.core.queue;

import ru.kwanza.jeda.api.IPriorityEvent;
import ru.kwanza.jeda.api.SinkException;

import java.util.*;

import static ru.kwanza.jeda.api.IPriorityEvent.Priority;

/**
 * @author Guzanov Alexander
 */
public class PriorityMemoryQueue<E extends IPriorityEvent> extends AbstractMemoryQueue<E> {
    private Map<Priority, Node> heads = new HashMap<Priority, Node>();
    private Map<Priority, Node> tails = new HashMap<Priority, Node>();

    public PriorityMemoryQueue() {
        this(Integer.MAX_VALUE);
    }

    public PriorityMemoryQueue(int maxSize) {
        super(maxSize);
        for (Priority p : Priority.values()) {
            Node lastNode;
            Node firstNode = lastNode = new Node(null);
            heads.put(p, firstNode);
            tails.put(p, lastNode);
        }

        this.maxSize = maxSize;
    }

    @Override
    public Collection<E> tryPut(Collection<E> events) throws SinkException {
        if (events.isEmpty()) {
            return null;
        }
        ArrayList<E> sortedEvents = new ArrayList<E>(events);
        Collections.sort(sortedEvents, PriorityEventComparator.INSTANCE);

        return super.tryPut(sortedEvents);
    }

    protected void addToTail(E e) {
        Node lastNode = tails.get(e.getPriority());
        lastNode = lastNode.next = new Node(e);
        tails.put(e.getPriority(), lastNode);
    }

    protected Collection<E> doTake(int c) {
        int takeCount = 0;
        MutableEventCollection<E> result = new MutableEventCollection<E>();
        for (Priority p : Priority.values()) {
            Node firstNode = heads.get(p);
            Node last = firstNode;
            while (takeCount < c && last.next != null) {
                last = last.next;
                result.add(last.event);
                takeCount++;
            }

            if (firstNode != last) {
                Node h = firstNode;
                h.next = h;     //help GC
                firstNode = last;
                last.event = null;
                heads.put(p, firstNode);
            }
            if (takeCount >= c) {
                break;
            }
        }
        return result;
    }
}
