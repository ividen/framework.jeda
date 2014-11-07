package ru.kwanza.jeda.clusterservice.impl.db;

import junit.framework.Assert;
import org.junit.Test;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.test.context.ContextConfiguration;
import ru.kwanza.jeda.clusterservice.impl.db.orm.ClusteredComponent;
import ru.kwanza.jeda.clusterservice.impl.db.orm.ClusterNode;
import ru.kwanza.toolbox.fieldhelper.FieldHelper;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Alexander Guzanov
 */
@ContextConfiguration(locations = "application-config_6.xml")
public class TestDBClusterService_6 extends AbstractDBClusterService {
    @Resource(name = "repair_module_1")
    private RepairableTestComponent m1;
    @Resource(name = "repair_module_2")
    private RepairableTestComponent m2;
    @Resource(name = "jeda.clusterservice.DBClusterService")
    private DBClusterService service;
    @Resource(name = "jeda.clusterservice.DBClusterService2")
    private DBClusterService service2;

    private static FieldHelper.Field<DBClusterService, ConcurrentMap<Integer, ConcurrentMap<DBClusterService.Supervisor.RepairWorker, ClusteredComponent>>>
            repairingNodes = FieldHelper.construct(DBClusterService.class, "supervisor.repairingNodes");

    @Test
    public void repairAndRecatchActivity() throws Exception {
        Thread.sleep(1000);
        Assert.assertEquals(true, m1.isStarted());
        Assert.assertEquals(false, m1.isRepairing());
        Assert.assertEquals(false, m1.isRepaired());
        Assert.assertEquals(false, m1.isStopped());
        Assert.assertEquals(true, m2.isStarted());
        Assert.assertEquals(false, m2.isRepairing());
        Assert.assertEquals(false, m2.isRepaired());
        Assert.assertEquals(false, m2.isStopped());

        Assert.assertEquals(2,service.getActiveNodes().size());
        Assert.assertEquals(2,service2.getActiveNodes().size());
        Assert.assertEquals(0,service.getPassiveNodes().size());
        Assert.assertEquals(0,service2.getPassiveNodes().size());

        service.destroy();
        Thread.sleep(1000);

        Assert.assertEquals(false, m1.isStarted());
        Assert.assertEquals(false, m1.isRepairing());
        Assert.assertEquals(false, m1.isRepaired());
        Assert.assertEquals(true, m1.isStopped());
        Assert.assertEquals(true, m2.isStarted());
        Assert.assertEquals(false, m2.isRepairing());
        Assert.assertEquals(false, m2.isRepaired());
        Assert.assertEquals(false, m2.isStopped());

        ClusterNode clusterNode = em.readByKey(ClusterNode.class, 1l);
        ClusteredComponent clusteredComponent = em.readByKey(ClusteredComponent.class, "1_repairable_module");
        clusterNode.setLastActivity(clusteredComponent.getLastActivity());

        em.update(clusterNode);

        Thread.sleep(1000);
        m2.setRepaired(false);

        Assert.assertEquals(1,service.getActiveNodes().size());
        Assert.assertEquals(1,service2.getActiveNodes().size());
        Assert.assertEquals(1,service.getPassiveNodes().size());
        Assert.assertEquals(1,service2.getPassiveNodes().size());;


        Assert.assertEquals(false, m1.isStarted());
        Assert.assertEquals(false, m1.isRepairing());
        Assert.assertEquals(false, m1.isRepaired());
        Assert.assertEquals(true, m1.isStopped());
        Assert.assertEquals(true, m2.isStarted());
        Assert.assertEquals(true, m2.isRepairing());
        Assert.assertEquals(false, m2.isRepaired());
        Assert.assertEquals(false, m2.isStopped());


        service.init();
        service.onApplicationEvent(new ContextRefreshedEvent(applicationContext));


        Thread.sleep(1000);

        Assert.assertEquals(true, m1.isStarted());
        Assert.assertEquals(false, m1.isRepairing());
        Assert.assertEquals(false, m1.isRepaired());
        Assert.assertEquals(false, m1.isStopped());
        Assert.assertEquals(true, m2.isStarted());
        Assert.assertEquals(true, m2.isRepairing());
        Assert.assertEquals(false, m2.isRepaired());
        Assert.assertEquals(false, m2.isStopped());

        Assert.assertTrue(repairingNodes.value(service2).isEmpty());
        Assert.assertEquals(2,service.getActiveNodes().size());
        Assert.assertEquals(2,service2.getActiveNodes().size());
        Assert.assertEquals(0,service.getPassiveNodes().size());
        Assert.assertEquals(0,service2.getPassiveNodes().size());;

    }

}
