package ru.kwanza.jeda.clusterservice.impl.db;

import junit.framework.Assert;
import org.dbunit.Assertion;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import ru.kwanza.jeda.clusterservice.impl.db.orm.ClusterNode;
import ru.kwanza.txn.api.spi.ITransactionManager;

import javax.annotation.Resource;

/**
 * @author Alexander Guzanov
 */
@ContextConfiguration(locations = "application-config_2.xml")
public class TestDBClusterService_2 extends AbstractDBClusterService {
    @Resource(name = "txn.ITransactionManager")
    private ITransactionManager tm;
    @Resource(name = "module_1")
    private TestComponent m1;
    @Resource(name = "module_2")
    private TestComponent m2;
    @Resource(name = "module_3")
    private TestComponent m3;

    @Test
    public void testCreateExistedNodeEntityAndModules() throws Exception {
        Assertion.assertEqualsIgnoreCols(getResourceSet("data_set_1.xml"),
                getActualDataSet("jeda_cluster_service"), "jeda_cluster_service", new String[]{"last_activity"});

        Assertion.assertEqualsIgnoreCols(getResourceSet("data_set_2.xml"),
                getActualDataSet("jeda_clustered_module"), "jeda_clustered_module", new String[]{"last_repaired"});
    }


    @Test
    public void testLostNodeActivity() throws Exception {
        ClusterNode clusterNode = em.readByKey(ClusterNode.class, 1l);

        while (true) {
            clusterNode = em.readByKey(ClusterNode.class, 1l);
            if (clusterNode.getLastActivity() > System.currentTimeMillis()) break;
        }

        Thread.sleep(1000);
        Assert.assertEquals(true, m1.isStarted());
        Assert.assertEquals(true, m2.isStarted());
        Assert.assertEquals(true, m3.isStarted());
        Assert.assertEquals(false, m1.isStopped());
        Assert.assertEquals(false, m2.isStopped());
        Assert.assertEquals(false, m3.isStopped());


        dbTool.getJdbcTemplate().execute("ALTER TABLE jeda_cluster_service RENAME TO jeda_cluster_service_1");

        Thread.sleep(1000);
        Assert.assertEquals(false, m1.isStarted());
        Assert.assertEquals(false, m2.isStarted());
        Assert.assertEquals(false, m3.isStarted());
        Assert.assertEquals(true, m1.isStopped());
        Assert.assertEquals(true, m2.isStopped());
        Assert.assertEquals(true, m3.isStopped());

        dbTool.getJdbcTemplate().execute("ALTER TABLE jeda_cluster_service_1 RENAME TO jeda_cluster_service");
        while (true) {
            clusterNode = em.readByKey(ClusterNode.class, 1l);
            if (clusterNode.getLastActivity() > System.currentTimeMillis()) break;
        }

        Thread.sleep(1000);
        Assert.assertEquals(true, m1.isStarted());
        Assert.assertEquals(true, m2.isStarted());
        Assert.assertEquals(true, m3.isStarted());
        Assert.assertEquals(false, m1.isStopped());
        Assert.assertEquals(false, m2.isStopped());
        Assert.assertEquals(false, m3.isStopped());
    }


    @Test
    public void testModuleLockException() throws Exception {
        ClusterNode clusterNode = em.readByKey(ClusterNode.class, 1l);

        while (true) {
            clusterNode = em.readByKey(ClusterNode.class, 1l);
            if (clusterNode.getLastActivity() > System.currentTimeMillis()) break;
        }

        Thread.sleep(1000);
        Assert.assertEquals(true, m1.isStarted());
        Assert.assertEquals(true, m2.isStarted());
        Assert.assertEquals(true, m3.isStarted());
        Assert.assertEquals(false, m1.isStopped());
        Assert.assertEquals(false, m2.isStopped());
        Assert.assertEquals(false, m3.isStopped());


        dbTool.getJdbcTemplate().execute("ALTER TABLE jeda_clustered_module RENAME TO jeda_clustered_module_1");

        Thread.sleep(1000);
        Assert.assertEquals(false, m1.isStarted());
        Assert.assertEquals(false, m2.isStarted());
        Assert.assertEquals(false, m3.isStarted());
        Assert.assertEquals(true, m1.isStopped());
        Assert.assertEquals(true, m2.isStopped());
        Assert.assertEquals(true, m3.isStopped());

        dbTool.getJdbcTemplate().execute("ALTER TABLE jeda_clustered_module_1 RENAME TO jeda_clustered_module");
        while (true) {
            clusterNode = em.readByKey(ClusterNode.class, 1l);
            if (clusterNode.getLastActivity() > System.currentTimeMillis()) break;
        }

        Thread.sleep(1000);
        Assert.assertEquals(true, m1.isStarted());
        Assert.assertEquals(true, m2.isStarted());
        Assert.assertEquals(true, m3.isStarted());
        Assert.assertEquals(false, m1.isStopped());
        Assert.assertEquals(false, m2.isStopped());
        Assert.assertEquals(false, m3.isStopped());
    }

}
