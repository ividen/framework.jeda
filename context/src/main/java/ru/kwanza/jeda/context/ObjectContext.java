package ru.kwanza.jeda.context;

import ru.kwanza.dbtool.core.VersionGenerator;
import ru.kwanza.jeda.api.IContext;
import ru.kwanza.toolbox.fieldhelper.FieldHelper;

public abstract class ObjectContext implements IContext<Long, Long> {

    protected Long id;
    protected Long version;

    protected ObjectContext() {
    }

    protected ObjectContext(Long id, Long version) {
        this.id = id;
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public static class VersionFieldImpl<T extends ObjectContext> implements FieldHelper.VersionField<T, Long> {

        private VersionGenerator versionGenerator;

        public VersionFieldImpl(VersionGenerator versionGenerator) {
            this.versionGenerator = versionGenerator;
        }

        public Long value(T object) {
            return object.version;
        }

        public Long generateNewValue(T object) {
            return versionGenerator.generate(MapContextImpl.class.getName(), object.version);
        }

        public void setValue(T object, Long value) {
            object.version = value;
        }
    }
}
