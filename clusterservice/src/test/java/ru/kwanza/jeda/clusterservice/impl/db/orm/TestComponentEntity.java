package ru.kwanza.jeda.clusterservice.impl.db.orm;

import junit.framework.Assert;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.Test;
import ru.kwanza.jeda.clusterservice.IClusteredComponent;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Alexander Guzanov
 */
public class TestComponentEntity {

    @Test
    public void testEntity(@Mocked final IClusteredComponent c){
        ComponentEntity test = new ComponentEntity(10, "test");
        Assert.assertEquals("10_test",test.getId());
        Assert.assertEquals("test",test.getName());
        Assert.assertFalse(test.getRepaired());
        Assert.assertFalse(test.getWaitForReturn());
        Assert.assertNull(test.getHoldNodeId());
        Assert.assertNull(test.getHoldNode());
        Assert.assertNull(test.getNode());
        Assert.assertEquals(10,test.getNodeId().intValue());
        long ts = System.currentTimeMillis();
        test.setLastActivity(ts);
        Assert.assertEquals(ts,test.getLastActivity().longValue());


        test.setHoldNodeId(200);
        test.setRepaired(true);
        test.setWaitForReturn(true);

        Assert.assertEquals(200,test.getHoldNodeId().intValue());
        Assert.assertTrue(test.getRepaired());
        Assert.assertTrue(test.getWaitForReturn());

        test.clearMarkers();
        Assert.assertFalse(test.getRepaired());
        Assert.assertFalse(test.getWaitForReturn());
        Assert.assertNull(test.getHoldNodeId());

        Assert.assertEquals(new ComponentEntity(10,"test"),test);
        Assert.assertEquals(new ComponentEntity(10,"test").hashCode(),test.hashCode());


        new Expectations(){{
            c.getName();result="test";
        }};
        final Collection<String> ids = ComponentEntity.getIds(1, Arrays.asList(c));
        Assert.assertEquals(1,ids.size());
        Assert.assertEquals("1_test",ids.iterator().next());


        Assert.assertEquals(true,Deencapsulation.getField(new WaitForReturnComponent("10_test",true),"waitForReturn"));

        Assert.assertEquals(true,Deencapsulation.getField(test.getWaitEntity(),"waitForReturn"));

    }

}
