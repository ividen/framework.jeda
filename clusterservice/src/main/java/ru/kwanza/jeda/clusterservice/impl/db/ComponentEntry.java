package ru.kwanza.jeda.clusterservice.impl.db;

import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.impl.db.orm.ComponentEntity;
import ru.kwanza.toolbox.fieldhelper.FieldHelper;

/**
 * @author Alexander Guzanov
 */
public class ComponentEntry{
    private IClusteredComponent component;
    private ComponentEntity entity;

    public static final FieldHelper.Field<ComponentEntry, IClusteredComponent> componentField
            = FieldHelper.construct(ComponentEntry.class, "component");

    public static final FieldHelper.Field<ComponentEntry, ComponentEntity> entityField
            = FieldHelper.construct(ComponentEntry.class, "entity");

    public ComponentEntry(IClusteredComponent component, ComponentEntity entity) {
        this.component = component;
        this.entity = entity;
    }
}
