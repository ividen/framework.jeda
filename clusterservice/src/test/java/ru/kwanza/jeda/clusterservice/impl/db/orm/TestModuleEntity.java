package ru.kwanza.jeda.clusterservice.impl.db.orm;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Alexander Guzanov
 */
public class TestModuleEntity {

    @Test
    public void testEntity(){
        ClusteredComponent test = new ClusteredComponent(10, "test");
        Assert.assertEquals("10_test",test.getId());
        Assert.assertEquals("test",test.getName());
        Assert.assertEquals(10,test.getNodeId().intValue());
        long ts = System.currentTimeMillis();
        test.setLastActivity(ts);
        Assert.assertEquals(ts,test.getLastActivity().longValue());

    }

}
