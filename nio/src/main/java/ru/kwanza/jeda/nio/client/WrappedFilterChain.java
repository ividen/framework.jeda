package ru.kwanza.jeda.nio.client;

import org.glassfish.grizzly.*;
import org.glassfish.grizzly.asyncqueue.PushBackHandler;
import org.glassfish.grizzly.filterchain.Filter;
import org.glassfish.grizzly.filterchain.FilterChain;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.FilterChainEvent;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Guzanov Alexander
 */
class WrappedFilterChain implements FilterChain {
    private ConnectionPool connectionPool;
    private FilterChain filterChain;

    WrappedFilterChain(FilterChain filterChain, ConnectionPool connectionPool) {
        this.filterChain = filterChain;
        this.connectionPool = connectionPool;
    }

    public int size() {
        return filterChain.size();
    }

    public boolean isEmpty() {
        return filterChain.isEmpty();
    }

    public boolean contains(Object o) {
        return filterChain.contains(o);
    }

    public Object[] toArray() {
        return filterChain.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return filterChain.toArray(a);
    }

    public boolean add(Filter filter) {
        return filterChain.add(filter);
    }

    public boolean remove(Object o) {
        return filterChain.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return filterChain.containsAll(c);
    }

    public boolean addAll(Collection<? extends Filter> c) {
        return filterChain.addAll(c);
    }

    public boolean removeAll(Collection<?> c) {
        return filterChain.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return filterChain.retainAll(c);
    }

    public void clear() {
        filterChain.clear();
    }

    public boolean equals(Object o) {
        return filterChain.equals(o);
    }

    public FilterChainContext obtainFilterChainContext(Connection connection) {
        return filterChain.obtainFilterChainContext(connection);
    }

    public int indexOfType(Class<? extends Filter> filterType) {
        return filterChain.indexOfType(filterType);
    }

    public ProcessorResult execute(FilterChainContext context) {
        return filterChain.execute(context);
    }

    public void flush(Connection connection, CompletionHandler<WriteResult> completionHandler) {
        filterChain.flush(connection, completionHandler);
    }

    public void fireEventUpstream(Connection connection, FilterChainEvent event, CompletionHandler<FilterChainContext> completionHandler) {
        filterChain.fireEventUpstream(connection, event, completionHandler);
    }

    public void fireEventDownstream(Connection connection, FilterChainEvent event, CompletionHandler<FilterChainContext> completionHandler) {
        filterChain.fireEventDownstream(connection, event, completionHandler);
    }

    public ReadResult read(FilterChainContext context) throws IOException {
        return filterChain.read(context);
    }

    public void fail(FilterChainContext context, Throwable failure) {
        filterChain.fail(context, failure);
    }

    public Iterator<Filter> iterator() {
        return filterChain.iterator();
    }

    public void add(int index, Filter element) {
        filterChain.add(index, element);
    }

    public Filter remove(int index) {
        return filterChain.remove(index);
    }

    public boolean addAll(int index, Collection<? extends Filter> c) {
        return filterChain.addAll(index, c);
    }

    public Filter get(int index) {
        return filterChain.get(index);
    }

    public Filter set(int index, Filter element) {
        return filterChain.set(index, element);
    }

    public int indexOf(Object o) {
        return filterChain.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return filterChain.lastIndexOf(o);
    }

    public ListIterator<Filter> listIterator() {
        return filterChain.listIterator();
    }

    public ListIterator<Filter> listIterator(int index) {
        return filterChain.listIterator(index);
    }

    public List<Filter> subList(int fromIndex, int toIndex) {
        return filterChain.subList(fromIndex, toIndex);
    }

    public void read(Connection connection, CompletionHandler<ReadResult> completionHandler) {
        filterChain.read(connection, completionHandler);
    }

    public Context obtainContext(Connection connection) {
        Context context = filterChain.obtainContext(connection);
        context.getAttributes().setAttribute(ConnectionPool.class.getName(), connectionPool);
        return context;
    }


    public ProcessorResult process(Context context) {
        context.getAttributes().setAttribute(ConnectionPool.class.getName(), connectionPool);
        return filterChain.process(context);
    }

    public void write(Connection connection, Object dstAddress, Object message, CompletionHandler<WriteResult> completionHandler, PushBackHandler pushBackHandler) {
        filterChain.write(connection, dstAddress, message, completionHandler, pushBackHandler);
    }

    public boolean isInterested(IOEvent ioEvent) {
        return filterChain.isInterested(ioEvent);
    }

    public void setInterested(IOEvent ioEvent, boolean isInterested) {
        filterChain.setInterested(ioEvent, isInterested);
    }

    public int hashCode() {
        return filterChain.hashCode();
    }
}
