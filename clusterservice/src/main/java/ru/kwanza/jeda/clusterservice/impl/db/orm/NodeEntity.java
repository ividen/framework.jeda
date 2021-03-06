package ru.kwanza.jeda.clusterservice.impl.db.orm;

import ru.kwanza.dbtool.orm.annotations.Entity;
import ru.kwanza.dbtool.orm.annotations.Field;
import ru.kwanza.dbtool.orm.annotations.IdField;
import ru.kwanza.jeda.clusterservice.Node;

/**
 * @author Alexander Guzanov
 */
@Entity(table = "jeda_cluster_node", name = "jeda.clusterservice.NodeEntity")
public class NodeEntity extends Node {
    public NodeEntity(Integer id, Long lastActivity, String ipAddress, String pid) {
        super(id);
        this.lastActivity = lastActivity;
        this.ipAddress = ipAddress;
        this.pid = pid;
    }

    @IdField("id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Field("last_activity")
    public Long getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Long value) {
        lastActivity = value;
    }

    @Field("ip_address")
    public String getIpAddress() {
        return ipAddress;
    }

    @Field("pid")
    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
