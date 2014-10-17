package ru.kwanza.jeda.clusterservice.impl.db.orm;

import ru.kwanza.dbtool.orm.annotations.Entity;
import ru.kwanza.dbtool.orm.annotations.Field;
import ru.kwanza.dbtool.orm.annotations.IdField;
import ru.kwanza.dbtool.orm.annotations.VersionField;
import ru.kwanza.jeda.clusterservice.IClusteredModule;
import ru.kwanza.toolbox.fieldhelper.FieldHelper;

import java.sql.Types;
import java.util.Collection;
import java.util.List;

/**
 * @author Alexander Guzanov
 */
@Entity(table = "jeda_clustered_module", name="jeda.clusterservice.ModuleEntity")
public class ModuleEntity {
    @IdField(value = "id", type = Types.VARCHAR)
    private String id;

    @Field("node_id")
    private Integer nodeId;

    @Field("name")
    private String name;

    @Field("las_repaired")
    private Long lastRepaired;

    public ModuleEntity(Integer nodeId, String name) {
        this.id = createId(nodeId, name);
        this.nodeId = nodeId;
        this.name = name;
        this.lastRepaired = System.currentTimeMillis();
    }

    public static String createId(Integer nodeId, String name) {
        return nodeId.toString() + "_" + name;
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

    public static Collection<String> getIds(final int nodeId,Collection<IClusteredModule> modules){
        return FieldHelper.getFieldCollection(modules,new FieldHelper.Field<IClusteredModule, String>() {
            public String value(IClusteredModule cm) {
                return createId(nodeId,cm.getName());
            }
        });
    }
}
