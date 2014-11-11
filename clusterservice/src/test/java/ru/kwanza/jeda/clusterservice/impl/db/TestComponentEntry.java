package ru.kwanza.jeda.clusterservice.impl.db;

import junit.framework.Assert;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.Test;
import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.impl.db.orm.ComponentEntity;

/**
 * @author Alexander Guzanov
 */
public class TestComponentEntry {

    @Test
    public void testEntry(@Mocked final IClusteredComponent component, @Mocked final ComponentEntity entity) {
        new Expectations(){{
                component.getName(); result = "testComponent";
                entity.getId();result="1_testComponent";
                entity.getName();result="testComponent";
                entity.getNodeId();result=1;
        }};

        final ComponentEntry entry = new ComponentEntry(component, entity);

        Assert.assertEquals("1_testComponent",ComponentEntry.entityField.value(entry).getId());
        Assert.assertEquals("testComponent",ComponentEntry.entityField.value(entry).getName());
        Assert.assertEquals(1,ComponentEntry.entityField.value(entry).getNodeId().intValue());

        Assert.assertEquals("testComponent",ComponentEntry.componentField.value(entry).getName());
    }
}
