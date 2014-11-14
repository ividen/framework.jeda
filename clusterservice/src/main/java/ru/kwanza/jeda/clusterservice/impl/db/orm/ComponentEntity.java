package ru.kwanza.jeda.clusterservice.impl.db.orm;

import ru.kwanza.dbtool.orm.annotations.Entity;
import ru.kwanza.dbtool.orm.annotations.Field;

/**
 * @author Alexander Guzanov
 */
@Entity(table = "jeda_clustered_component", name = "jeda.clusterservice.ComponentEntity")
public class ComponentEntity extends BaseComponentEntity {
    @Field("wait_for_return")
    private Boolean waitForReturn;

    public ComponentEntity(Integer nodeId, String name) {
        super(nodeId, name);
        this.waitForReturn = false;
    }

    public WaitForReturnComponent getWaitFoReturn() {
        return new WaitForReturnComponent(id, true);
    }

    public AlienComponent getAlien() {
        return new AlienComponent(this);
    }

    public Boolean getWaitForReturn() {
        return waitForReturn;
    }

    public void setWaitForReturn(Boolean waitForReturn) {
        this.waitForReturn = waitForReturn;
    }

    @Override
    public void clearMarkers() {
        super.clearMarkers();
        this.waitForReturn = false;
    }
}
