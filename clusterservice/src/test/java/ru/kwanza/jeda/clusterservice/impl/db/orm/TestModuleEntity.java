package ru.kwanza.jeda.clusterservice.impl.db.orm;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Alexander Guzanov
 */
public class TestModuleEntity {

    @Test
    public void testEntity(){
        ModuleEntity test = new ModuleEntity(10, "test");
        Assert.assertEquals("10_test",test.getId());
        Assert.assertEquals("test",test.getName());
        Assert.assertEquals(10,test.getNodeId().intValue());
        long ts = System.currentTimeMillis();
        test.setLastRepaired(ts);
        Assert.assertEquals(ts,test.getLastRepaired().longValue());

    }

}
