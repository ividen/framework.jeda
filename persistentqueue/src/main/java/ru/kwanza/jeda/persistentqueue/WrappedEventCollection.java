package ru.kwanza.jeda.persistentqueue;

import ru.kwanza.jeda.api.IEvent;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Guzanov Alexander
 */
class WrappedEventCollection<E extends IEvent> extends AbstractCollection<E> {
    private Collection<EventWithKey> delegate;

    private class WrappedEventIterator<E extends IEvent> implements Iterator<E> {
        private Iterator<EventWithKey> iterator;

        private WrappedEventIterator(Iterator<EventWithKey> iterator) {
            this.iterator = iterator;
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public E next() {
            return (E) iterator.next().getDelegate();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public WrappedEventCollection(Collection<EventWithKey> delegate) {
        this.delegate = delegate;
    }

    public int size() {
        return delegate.size();
    }

    @Override
    public Iterator<E> iterator() {
        return new WrappedEventIterator<E>(delegate.iterator());
    }
}
