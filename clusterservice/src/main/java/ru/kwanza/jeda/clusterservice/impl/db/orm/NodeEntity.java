package ru.kwanza.jeda.clusterservice.impl.db.orm;

import ru.kwanza.dbtool.orm.annotations.Entity;
import ru.kwanza.dbtool.orm.annotations.Field;
import ru.kwanza.dbtool.orm.annotations.VersionField;
import ru.kwanza.jeda.clusterservice.Node;

/**
 * @author Alexander Guzanov
 */
@Entity(table = "cluster_service_nodes")
public class NodeEntity extends Node {
//    @VersionField("version")
//    private Long version;

    public NodeEntity(Integer id, Long lastActivity) {
        this.id = id;
        this.lastActivity = lastActivity;
    }

    @Field("id")
    public Integer getId() {
        return id;
    }

    @Field("last_activity")
    public Long getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Long value) {
        lastActivity = value;
    }
}
