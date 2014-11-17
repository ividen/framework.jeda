package ru.kwanza.jeda.clusterservice.impl.db.orm;

import ru.kwanza.dbtool.orm.annotations.Entity;

/**
 * @author Alexander Guzanov
 */
@Entity(table = "jeda_clustered_component", name = "jeda.clusterservice.AlienComponent")
public class AlienComponent extends BaseComponentEntity{
    private transient boolean markRepaired = false;

    public AlienComponent(ComponentEntity entity) {
        super(entity.getNodeId(), entity.getName());
        this.repaired = false;
        this.lastActivity = entity.lastActivity;
        this.version = entity.version;
        this.nodeId = entity.nodeId;
        this.holdNodeId = entity.nodeId;
        this.node = entity.node;
    }

    public boolean isMarkRepaired() {
        return markRepaired;
    }

    public void setMarkRepaired(boolean markRepaired) {
        this.markRepaired = markRepaired;
    }
}
