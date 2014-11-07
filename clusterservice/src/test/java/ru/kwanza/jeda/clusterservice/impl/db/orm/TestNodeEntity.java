package ru.kwanza.jeda.clusterservice.impl.db.orm;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Alexander Guzanov
 */
public class TestNodeEntity {

    @Test
    public void testEntity(){
        long lastActivity = System.currentTimeMillis();
        ClusterNode test = new ClusterNode(10, lastActivity);

        Assert.assertEquals(10,test.getId().intValue());
        Assert.assertEquals(lastActivity,test.getLastActivity().longValue());

        test.setId(100);
        test.setLastActivity(10000l);
        Assert.assertEquals(100,test.getId().intValue());
        Assert.assertEquals(10000l,test.getLastActivity().longValue());

    }
}
