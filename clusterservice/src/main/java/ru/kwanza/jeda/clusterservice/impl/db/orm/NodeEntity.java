package ru.kwanza.jeda.clusterservice.impl.db.orm;

import ru.kwanza.dbtool.orm.annotations.Entity;
import ru.kwanza.dbtool.orm.annotations.Field;
import ru.kwanza.dbtool.orm.annotations.IdField;
import ru.kwanza.dbtool.orm.annotations.VersionField;
import ru.kwanza.jeda.clusterservice.Node;

/**
 * @author Alexander Guzanov
 */
@Entity(table = "jeda_cluster_service",  name="jeda.clusterservice.NodeEntity")
public class NodeEntity extends Node {
    public NodeEntity(Integer id, Long lastActivity) {
        super(id);
        this.lastActivity = lastActivity;
    }

    @IdField("id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id){
        this.id = id;
    }

    @Field("last_activity")
    public Long getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Long value) {
        lastActivity = value;
    }
}
