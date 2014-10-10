package ru.kwanza.jeda.clusterservice.impl.db.orm;

import ru.kwanza.dbtool.orm.annotations.Entity;
import ru.kwanza.dbtool.orm.annotations.Field;
import ru.kwanza.dbtool.orm.annotations.IdField;
import ru.kwanza.dbtool.orm.annotations.VersionField;

/**
 * @author Alexander Guzanov
 */
@Entity(table = "cluster_service_module")
public class ModuleEntity {
    @IdField("id")
    private String id;

    @Field("node_id")
    private Integer nodeId;

    @Field("name")
    private String name;

    @Field("lastRepaired")
    private Long lastRepaired;
    @VersionField("version")
    private Long version;

    public ModuleEntity(Integer nodeId, String name) {
        this.id = nodeId.toString() + "_" + name;
        this.nodeId = nodeId;
        this.name = name;
        this.lastRepaired = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public String getName() {
        return name;
    }

    public Long getLastRepaired() {
        return lastRepaired;
    }

    public void setLastRepaired(Long lastRepaired) {
        this.lastRepaired = lastRepaired;
    }
}
