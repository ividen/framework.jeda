package ru.kwanza.jeda.core.queue;

import ru.kwanza.jeda.api.IEvent;

import java.util.AbstractCollection;
import java.util.Iterator;

/**
 * @author: Guzanov Alexander
 */
class ImmutableEventCollection<E extends IEvent> extends AbstractCollection<E> {
    private int size;
    private Node<E> firstNode;

    private final class EventIterator implements Iterator<E> {
        private Node<E> currentNode;
        private int counter;

        public EventIterator() {
            this.counter = size;
        }

        public boolean hasNext() {
            return counter > 0;
        }

        public E next() {
            if (counter <= 0) {
                throw new IllegalStateException("Iterator finished!");
            }

            currentNode = currentNode != null ? currentNode.next : firstNode;
            counter--;

            return currentNode.event;
        }

        public void remove() {
            throw new UnsupportedOperationException("This collection is immutable!");
        }
    }

    ImmutableEventCollection(Node<E> firstNode, int size) {
        this.firstNode = firstNode;
        this.size = size;
    }

    public int size() {
        return size;
    }

    public Iterator<E> iterator() {
        return new EventIterator();
    }
}
