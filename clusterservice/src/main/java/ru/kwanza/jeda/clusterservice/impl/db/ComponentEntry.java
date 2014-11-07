package ru.kwanza.jeda.clusterservice.impl.db;

import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.impl.db.orm.ClusteredComponent;

import java.util.UUID;

/**
 * @author Alexander Guzanov
 */
public class ComponentEntry {
    private IClusteredComponent component;
    private ClusteredComponent entity;

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
