package ru.kwanza.jeda.clusterservice.impl.db.orm;

import ru.kwanza.dbtool.orm.annotations.Entity;
import ru.kwanza.dbtool.orm.annotations.Field;

/**
 * @author Alexander Guzanov
 */
@Entity(table = "jeda_clustered_component", name = "jeda.clusterservice.ComponentEntity")
public class ComponentEntity extends BaseComponentEntity {
    @Field("wait_for_return")
    private Long waitForReturn;

    public ComponentEntity(Integer nodeId, String name) {
        super(nodeId, name);
        this.waitForReturn = null;
    }

    public Long getWaitForReturn() {
        return waitForReturn;
    }

    public void setWaitForReturn(Long waitForReturn) {
        this.waitForReturn = waitForReturn;
    }

    @Override
    public void clearMarkers() {
        super.clearMarkers();
        this.waitForReturn = null;
    }
}
