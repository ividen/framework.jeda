package ru.kwanza.jeda.core.queue;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.ISystemManager;
import ru.kwanza.jeda.api.internal.ISystemManagerInternal;

import java.util.ArrayList;


/**
 * @author Guzanov Alexander
 */
public class TransactionalMemoryQueue<E extends IEvent> extends AbstractTransactionalMemoryQueue<E> {
    private Node firstNode;
    private Node lastNode;

    public TransactionalMemoryQueue(ISystemManagerInternal manager) {
        this(manager, ObjectCloneType.SERIALIZE, Long.MAX_VALUE);
    }

    public TransactionalMemoryQueue(ISystemManagerInternal manager, ObjectCloneType objectCloneType, long maxSize) {
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
