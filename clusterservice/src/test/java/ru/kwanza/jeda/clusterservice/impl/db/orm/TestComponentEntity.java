package ru.kwanza.jeda.clusterservice.impl.db.orm;

import junit.framework.Assert;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.Test;
import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.Node;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Alexander Guzanov
 */
public class TestComponentEntity {

    @Test
    public void testEntity(@Mocked final IClusteredComponent c) {
        ComponentEntity test = new ComponentEntity(10, "test");
        Assert.assertEquals("10_test", test.getId());
        Assert.assertEquals("test", test.getName());
        Assert.assertFalse(test.getRepaired());
        Assert.assertNull(test.getWaitForReturn());
        Assert.assertNull(test.getHoldNodeId());
        Assert.assertNull(test.getNode());
        Assert.assertEquals(10, test.getNodeId().intValue());
        long ts = System.currentTimeMillis();
        test.setLastActivity(ts);
        Assert.assertEquals(ts, test.getLastActivity().longValue());


        test.setHoldNodeId(200);
        test.setRepaired(true);
        test.setWaitForReturn(999l);

        Assert.assertEquals(200, test.getHoldNodeId().intValue());
        Assert.assertTrue(test.getRepaired());
        Assert.assertEquals(999l, test.getWaitForReturn().longValue());

        test.clearMarkers();
        Assert.assertFalse(test.getRepaired());
        Assert.assertNull(test.getWaitForReturn());
        Assert.assertNull(test.getHoldNodeId());

        Assert.assertEquals(new ComponentEntity(10, "test"), test);
        Assert.assertEquals(new ComponentEntity(10, "test").hashCode(), test.hashCode());


        new Expectations() {{
            c.getName();
            result = "test";
        }};
        final Collection<String> ids = ComponentEntity.getIds(1, Arrays.asList(c));
        Assert.assertEquals(1, ids.size());
        Assert.assertEquals("1_test", ids.iterator().next());


        Assert.assertEquals(100l, Deencapsulation.getField(new WaitForReturnComponent("10_test", 100l), "waitForReturn"));
    }

    @Test
    public void testCreateId(@Mocked final Node node, @Mocked final IClusteredComponent cmp){
        new Expectations(){{
            node.getId();result=1;
            cmp.getName();result="test_cmp";
        }};

        Assert.assertEquals("1_test_cmp",BaseComponentEntity.createId(node,cmp));
        Assert.assertEquals("1_test_cmp",BaseComponentEntity.createId(node.getId(),cmp.getName()));
    }

    @Test
    public void testEquals(){
         Assert.assertEquals(new ComponentEntity(1,"name"),new ComponentEntity(1,"name"));
         Assert.assertTrue(!new ComponentEntity(1, "name").equals(new ComponentEntity(1, "name1")));
    }
    @Test
    public void tsetWaiteForReturlnjn(){
        Assert.assertEquals(666l, Deencapsulation.getField(new WaitForReturnComponent("111",666l),"waitForReturn"));
        Assert.assertEquals("111", Deencapsulation.getField(new WaitForReturnComponent("111",666l),"id"));
    }

    @Test
    public void testAlienConponent(){
        final ComponentEntity testComonent = new ComponentEntity(1, "testComonent");
        testComonent.setHoldNodeId(2);
        testComonent.setLastActivity(1000l);
        testComonent.setRepaired(true);

        final AlienComponent alienComponent = new AlienComponent(testComonent);
        Assert.assertEquals("1_testComonent",alienComponent.getId());
        Assert.assertEquals(1,alienComponent.getNodeId().intValue());
        Assert.assertEquals("testComonent",alienComponent.getName());
        Assert.assertEquals(1000l, alienComponent.getLastActivity().longValue());
        Assert.assertEquals(2,alienComponent.getHoldNodeId().intValue());
        Assert.assertEquals(false, alienComponent.getRepaired().booleanValue());

        Assert.assertEquals(false, alienComponent.isMarkRepaired());
        alienComponent.setMarkRepaired(true);
        Assert.assertEquals(true, alienComponent.isMarkRepaired());

    }
}
