package ru.kwanza.jeda.core.queue;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.IJedaManager;

import java.util.ArrayList;


/**
 * @author Guzanov Alexander
 */
public class TransactionalMemoryQueue<E extends IEvent> extends AbstractTransactionalMemoryQueue<E> {
    private Node firstNode;
    private Node lastNode;

    public TransactionalMemoryQueue(IJedaManager manager) {
        this(manager, ObjectCloneType.SERIALIZE, Integer.MAX_VALUE);
    }

    public TransactionalMemoryQueue(IJedaManager manager, ObjectCloneType objectCloneType, int maxSize) {
        super(manager, objectCloneType, maxSize);
        lastNode = firstNode = new Node(null);
    }

    @Override
    protected void addToHead(E e) {
        firstNode.event = e;
        firstNode = new Node<E>(null, firstNode);
    }

    protected void addToTail(E e) {
        lastNode = lastNode.next = new Node<E>(e);
    }

    protected void processTake(Tx tx, int c, ArrayList<E> result, ObjectOutputStreamEx oos) {
        Node last = firstNode;
        last = iterateOverNodeAndTake(last, tx, c, result, oos);


        if (last != firstNode) {
            firstNode = last;
            last.event = null;
        }
    }
}
