package ru.kwanza.jeda.clusterservice.impl.db.orm;

import ru.kwanza.dbtool.orm.annotations.Field;
import ru.kwanza.dbtool.orm.annotations.IdField;
import ru.kwanza.dbtool.orm.annotations.ManyToOne;
import ru.kwanza.dbtool.orm.annotations.VersionField;
import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.toolbox.fieldhelper.FieldHelper;

import java.sql.Types;
import java.util.Collection;

/**
 * @author Alexander Guzanov
 */
public class BaseComponentEntity {
    @IdField(value = "id", type = Types.VARCHAR)
    protected String id;
    @Field("name")
    protected String name;
    @Field("node_id")
    protected Integer nodeId;
    @Field("hold_node_id")
    protected Integer holdNodeId;
    @Field("repaired")
    protected Boolean repaired;
    @Field("last_activity")
    protected Long lastActivity;
    @VersionField("version")
    protected Long version;

    @ManyToOne(property = "nodeId")
    protected NodeEntity node;

    public BaseComponentEntity(Integer nodeId, String name) {
        this.id = createId(nodeId, name);
        this.nodeId = nodeId;
        this.name = name;
        this.lastActivity = System.currentTimeMillis();
        this.repaired = false;
    }

    public static String createId(Integer nodeId, String name) {
        return nodeId.toString() + "_" + name;
    }

    public static String createId(Node node, IClusteredComponent component) {
        return node.getId() + "_" + component.getName();
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

    public Long getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Long lastActivity) {
        this.lastActivity = lastActivity;
    }

    public Integer getHoldNodeId() {
        return holdNodeId;
    }

    public void setHoldNodeId(Integer holdNodeId) {
        this.holdNodeId = holdNodeId;
    }

    public Boolean getRepaired() {
        return repaired;
    }

    public void setRepaired(Boolean repaired) {
        this.repaired = repaired;
    }

    public NodeEntity getNode() {
        return node;
    }

    public static Collection<String> getIds(final int nodeId, Collection<IClusteredComponent> modules) {
        return FieldHelper.getFieldCollection(modules, new FieldHelper.Field<IClusteredComponent, String>() {
            public String value(IClusteredComponent cm) {
                return createId(nodeId, cm.getName());
            }
        });
    }

    public void clearMarkers(){
        this.holdNodeId = null;
        this.repaired = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof BaseComponentEntity)) return false;

        BaseComponentEntity component = (BaseComponentEntity) o;

        if (id != null ? !id.equals(component.id) : component.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
