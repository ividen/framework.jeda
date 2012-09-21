package ru.kwanza.jeda.context;

import ru.kwanza.dbtool.VersionGenerator;
import ru.kwanza.jeda.api.IMapContext;
import ru.kwanza.toolbox.fieldhelper.FieldHelper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapContextImpl implements IMapContext<String, Long, String, Object> {

    private String id;
    private String terminator;
    private Long version;
    private Map<String, Object> valueByKey = new HashMap<String, Object>();

    public MapContextImpl(String id, String terminator, Long version) {
        this.id = id;
        this.terminator = terminator;
        this.version = version;
    }

    public MapContextImpl(String id, String terminator, Long version, Map<String, Object> valueByKey) {
        this(id, terminator, version);
        this.valueByKey.putAll(valueByKey);
    }

    public Map<String, Object> getInnerMap() {
        return valueByKey;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTerminator() {
        return terminator;
    }

    public void setTerminator(String terminator) {
        this.terminator = terminator;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public int size() {
        return valueByKey.size();
    }

    public boolean isEmpty() {
        return valueByKey.isEmpty();
    }

    public boolean containsKey(Object key) {
        return valueByKey.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return valueByKey.containsValue(value);
    }

    public Object get(Object key) {
        return valueByKey.get(key);
    }

    public Object put(String key, Object value) {
        return valueByKey.put(key, value);
    }

    public Object remove(Object key) {
        return valueByKey.remove(key);
    }

    public void putAll(Map<? extends String, ?> m) {
        valueByKey.putAll(m);
    }

    public void clear() {
        valueByKey.clear();
    }

    public Set<String> keySet() {
        return valueByKey.keySet();
    }

    public Collection<Object> values() {
        return valueByKey.values();
    }

    public Set<Entry<String, Object>> entrySet() {
        return valueByKey.entrySet();
    }
    public static final FieldHelper.Field<MapContextImpl, String> KEY = new FieldHelper.Field<MapContextImpl, String>() {
        public String value(MapContextImpl object) {
            return object.id;
        }
    };

    public static class VersionFieldImpl implements FieldHelper.VersionField<MapContextImpl, Long> {

        private VersionGenerator versionGenerator;

        public VersionFieldImpl(VersionGenerator versionGenerator) {
            this.versionGenerator = versionGenerator;
        }

        public Long value(MapContextImpl object) {
            return object.version;
        }

        public Long generateNewValue(MapContextImpl object) {
            return versionGenerator.generate(MapContextImpl.class.getName(), object.version);
        }

        public void setValue(MapContextImpl object, Long value) {
            object.version = value;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MapContextImpl that = (MapContextImpl) o;

        return id.equals(that.id)
                && !(terminator != null ? !terminator.equals(that.terminator) : that.terminator != null)
                && valueByKey.equals(that.valueByKey)
                && !(version != null ? !version.equals(that.version) : that.version != null);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (terminator != null ? terminator.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + valueByKey.hashCode();
        return result;
    }
}
