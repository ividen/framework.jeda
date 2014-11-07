package ru.kwanza.jeda.clusterservice.impl.db;

import junit.framework.Assert;
import org.dbunit.Assertion;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Alexander Guzanov
 */
@ContextConfiguration(locations = "application-config_1.xml")
public class TestDBClusterService_1 extends AbstractDBClusterService {

    @Test
    public void testPropertySet() throws Exception {
        Assert.assertEquals(1,service.getCurrentNodeId().intValue());
        Assert.assertEquals(60000,service.getFailoverInterval());
        Assert.assertEquals(100,service.getActivityInterval());
        Assert.assertEquals(1,service.getRepairThreadCount());
    }

    @Test
    public void testCreateNodeEntityAndModules() throws Exception {
        Assertion.assertEqualsIgnoreCols(getResourceSet("data_set_1.xml"),
                getActualDataSet("jeda_cluster_service"), "jeda_cluster_service", new String[]{"last_activity"});

        Assertion.assertEqualsIgnoreCols(getResourceSet("data_set_2.xml"),
                getActualDataSet("jeda_clustered_module"), "jeda_clustered_module", new String[]{"last_repaired"});
    }

    @Test
    public void testRegisterAfterCreate() throws Exception {
        try {
            service.registerComponent(new TestComponent("module_4"));
            Assert.fail("Expected " + IllegalStateException.class);
        } catch (IllegalStateException e) {

        }
        Assertion.assertEqualsIgnoreCols(getResourceSet("data_set_2.xml"),
                getActualDataSet("jeda_clustered_module"), "jeda_clustered_module", new String[]{"last_repaired"});
    }


}
