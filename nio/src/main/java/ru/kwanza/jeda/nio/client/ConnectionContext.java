package ru.kwanza.jeda.nio.client;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.attributes.Attribute;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Guzanov Alexander
 */
public class ConnectionContext implements Map<Object, Object> {
    private ConcurrentHashMap map = new ConcurrentHashMap();
    private ITransportEvent requestEvent;

    private static Attribute CONNECTION_CONTEXT = Grizzly.DEFAULT_ATTRIBUTE_BUILDER.createAttribute("jeda-nio:ConnectionContext");

    public ConnectionContext() {
    }

    public ConnectionContext(ITransportEvent requestEvent) {
        this.requestEvent = requestEvent;
    }

    public ITransportEvent getRequestEvent() {
        return requestEvent;
    }

    void setRequestEvent(ITransportEvent requestEvent) {
        if (this.requestEvent != null) {
            throw new IllegalStateException("Previous request must be cleared first");
        }
        this.requestEvent = requestEvent;
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public int size() {
        return map.size();
    }

    public Object get(Object key) {
        return map.get(key);
    }

    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public Object put(Object key, Object value) {
        return map.put(key, value);
    }

    public void putAll(Map m) {
        map.putAll(m);
    }

    public Object remove(Object key) {
        return map.remove(key);
    }


    public void clear() {
        map.clear();
        this.requestEvent = null;
    }

    public Set keySet() {
        return map.keySet();
    }

    public Collection values() {
        return map.values();
    }

    public Set entrySet() {
        return map.entrySet();
    }

    public Enumeration keys() {
        return map.keys();
    }

    public Enumeration elements() {
        return map.elements();
    }

    public boolean equals(Object o) {
        return map.equals(o);
    }

    public int hashCode() {
        return map.hashCode();
    }

    public String toString() {
        return map.toString();
    }

    public static ConnectionContext getContext(Connection connection) {
        return (ConnectionContext) CONNECTION_CONTEXT.get(connection);
    }

    public static ConnectionContext create(Connection connection, ITransportEvent event) {
        ConnectionContext context = new ConnectionContext(event);
        CONNECTION_CONTEXT.set(connection, context);
        return context;
    }
}
