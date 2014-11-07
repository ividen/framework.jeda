package ru.kwanza.jeda.clusterservice.impl.db;

import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.impl.db.orm.ClusteredComponent;
import ru.kwanza.toolbox.fieldhelper.FieldHelper;

/**
 * @author Alexander Guzanov
 */
public class ComponentEntry {
    private IClusteredComponent component;
    private ClusteredComponent entity;

    public static final FieldHelper.Field<ComponentEntry, IClusteredComponent> componentField
            = FieldHelper.construct(ComponentEntry.class, "component");

    public static final FieldHelper.Field<ComponentEntry, ClusteredComponent> entityField
            = FieldHelper.construct(ComponentEntry.class, "entity");

    public ComponentEntry(IClusteredComponent component, ClusteredComponent entity) {
        this.component = component;
        this.entity = entity;
    }

    public IClusteredComponent getComponent() {
        return component;
    }

    public ClusteredComponent getEntity() {
        return entity;
    }

    public void setEntity(ClusteredComponent entity) {
        this.entity = entity;
    }

}
