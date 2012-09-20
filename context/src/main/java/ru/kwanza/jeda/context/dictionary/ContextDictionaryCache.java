package ru.kwanza.jeda.context.dictionary;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Dmitry Zagorovsky
 */
public class ContextDictionaryCache {

    private final ConcurrentHashMap<String, Long> propIdByName = new ConcurrentHashMap<String, Long>();
    private final ConcurrentHashMap<Long, String> propNameById = new ConcurrentHashMap<Long, String>();

    public Long getPropertyId(String propertyName) {
        return propIdByName.get(propertyName);
    }

    public String getPropertyName(Long propertyId) {
        return propNameById.get(propertyId);
    }

    public void put(String propertyName, Long id) {
        propIdByName.put(propertyName, id);
        propNameById.put(id, propertyName);
    }

    public void putIdByName(Map<String, Long> propIdByName) {
        this.propIdByName.putAll(propIdByName);
        for (Map.Entry<String, Long> entry : propIdByName.entrySet()) {
            propNameById.put(entry.getValue(), entry.getKey());
        }
    }

    // Test methods.
    ConcurrentHashMap<String, Long> getPropIdByName() {
        return propIdByName;
    }

    ConcurrentHashMap<Long, String> getPropNameById() {
        return propNameById;
    }

}
