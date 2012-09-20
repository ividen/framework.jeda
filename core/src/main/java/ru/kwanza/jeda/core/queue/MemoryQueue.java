package ru.kwanza.jeda.core.queue;

import ru.kwanza.jeda.api.IEvent;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class MemoryQueue<E extends IEvent> extends AbstractMemoryQueue<E> {
    private Node firstNode;
    private Node lastNode;

    public MemoryQueue() {
        this(Long.MAX_VALUE);
    }

    public MemoryQueue(long maxSize) {
        super(maxSize);
        lastNode = firstNode = new Node(null);
    }

    protected void addToTail(E e) {
        lastNode = lastNode.next = new Node<E>(e);
    }

    protected Collection<E> doTake(int c) {
        Node last = firstNode;
        Node prev = last;
        for (int i = 0; i < c && last.next != null; i++) {
            prev = last;
            last = last.next;
        }

        ImmutableEventCollection result;

        if (prev != firstNode) {
            result = new ImmutableEventCollection<E>(firstNode.next, c);
            prev.next = new Node(last.event);
        } else {
            result = new ImmutableEventCollection<E>(new Node(last.event), c);
        }

        Node h = firstNode;
        h.next = h;
        firstNode = last;
        last.event = null;
        return result;
    }
}
