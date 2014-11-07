package ru.kwanza.jeda.clusterservice.impl.db.orm;

import ru.kwanza.dbtool.orm.annotations.Entity;
import ru.kwanza.dbtool.orm.annotations.Field;
import ru.kwanza.dbtool.orm.annotations.IdField;

import java.sql.Types;

/**
 * @author Alexander Guzanov
 */
@Entity(table = "jeda_clustered_component", name = "jeda.clusterservice.HoldedClusteredComponent")
public class WaitForReturnComponent {
    @IdField(value = "id", type = Types.VARCHAR)
    private String id;
    @Field("wait_for_return")
    private Boolean waitForReturn;

    public WaitForReturnComponent(String id, Boolean waitForReturn) {
        this.id = id;
        this.waitForReturn = waitForReturn;
    }
}
