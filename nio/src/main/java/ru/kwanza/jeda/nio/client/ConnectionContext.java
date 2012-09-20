package ru.kwanza.jeda.nio.client;

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

    public ConnectionContext() {
    }

    public ITransportEvent getRequestEvent() {
        return requestEvent;
    }

    void setRequestEvent(ITransportEvent requestEvent) {
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
}
