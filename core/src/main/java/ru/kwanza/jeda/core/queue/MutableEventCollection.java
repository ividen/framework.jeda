package ru.kwanza.jeda.core.queue;

import ru.kwanza.jeda.api.IEvent;

import java.util.AbstractCollection;
import java.util.Iterator;

/**
 * @author: Guzanov Alexander
 */
class MutableEventCollection<E extends IEvent> extends AbstractCollection<E> {
    private int size = 0;
    private Node firstNode;
    private Node lastNode;

    private final class EventIterator implements Iterator<E> {
        private Node currentNode;
        private Node prevNode;
        private boolean markRemove;
        private int counter;

        public EventIterator() {
            this.counter = size;
        }

        public boolean hasNext() {
            return counter > 0;
        }

        public E next() {
            if (!markRemove) {
                prevNode = currentNode;
                currentNode = currentNode != null ? currentNode.next : firstNode;
            } else {
                markRemove = false;
            }
            counter--;

            return (E) currentNode.event;
        }

        public void remove() {
            if (currentNode == null) {
                throw new IllegalStateException("Iterator doesn't point to any item! ");
            }
            if (markRemove) {
                throw new IllegalStateException("Element already marked for deletion!");
            }
            markRemove = true;
            if (prevNode == null) {
                firstNode = firstNode.next;
                currentNode = firstNode;
            } else {
                prevNode.next = currentNode.next;
                currentNode = currentNode.next;
            }

            size--;
        }
    }

    MutableEventCollection() {
    }

    public boolean add(IEvent event) {
        Node node = new Node(event);
        if (firstNode != null) {
            lastNode.next = node;
            lastNode = node;
        } else {
            firstNode = lastNode = node;
        }
        size++;
        return true;
    }

    public int size() {
        return size;
    }

    public Iterator<E> iterator() {
        return new EventIterator();
    }
}
