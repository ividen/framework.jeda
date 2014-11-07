package ru.kwanza.jeda.clusterservice.impl.db;

import junit.framework.Assert;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import ru.kwanza.dbtool.orm.api.LockResult;
import ru.kwanza.dbtool.orm.api.LockType;
import ru.kwanza.jeda.clusterservice.impl.db.orm.ClusteredComponent;
import ru.kwanza.toolbox.fieldhelper.FieldHelper;
import ru.kwanza.txn.api.spi.ITransactionManager;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Alexander Guzanov
 */
@ContextConfiguration(locations = "application-config_5.xml")
public class TestDBClusterService_5 extends AbstractDBClusterService {
    @Resource(name = "repair_module_1")
    private RepairableTestComponent m1;
    @Resource(name = "repair_module_2")
    private RepairableTestComponent m2;
    @Resource(name = "repair_module_3")
    private RepairableTestComponent m3;
    @Resource(name = "jeda.clusterservice.DBClusterService")
    private DBClusterService service;
    @Resource(name = "jeda.clusterservice.DBClusterService2")
    private DBClusterService service2;
    @Resource(name = "jeda.clusterservice.DBClusterService3")
    private DBClusterService service3;
    @Resource(name = "txn.ITransactionManager")
    private ITransactionManager tm;

    private static FieldHelper.Field<DBClusterService, ConcurrentMap<Integer, ConcurrentMap<DBClusterService.Supervisor.RepairWorker, ClusteredComponent>>>
            repairingNodes = FieldHelper.construct(DBClusterService.class, "supervisor.repairingNodes");


    @Test
    public void testCritical_1() throws InvocationTargetException, InterruptedException {
        final boolean noLock = service.criticalSection(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                ClusteredComponent clusteredComponent = em.readByKey(ClusteredComponent.class, "1_repairable_module");
                final LockResult<ClusteredComponent> lock = em.lock(LockType.SKIP_LOCKED, clusteredComponent);

                return lock.getLocked().isEmpty();
            }
        });

        Assert.assertEquals(true, noLock);
    }


    @Test
    public void testCritical_2() throws InvocationTargetException, InterruptedException, TimeoutException {
        final boolean noLock = service.criticalSection(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                ClusteredComponent clusteredComponent = em.readByKey(ClusteredComponent.class, "1_repairable_module");
                final LockResult<ClusteredComponent> lock = em.lock(LockType.SKIP_LOCKED, clusteredComponent);

                return lock.getLocked().isEmpty();
            }
        }, 1000l, TimeUnit.MILLISECONDS);

        Assert.assertEquals(true, noLock);
    }

    @Test(expected = TimeoutException.class)
    public void testCritical_3() throws InvocationTargetException, InterruptedException, TimeoutException {
        ClusteredComponent clusteredComponent = em.readByKey(ClusteredComponent.class, "1_repairable_module");
        tm.begin();

        final LockResult<ClusteredComponent> lock = em.lock(LockType.WAIT, clusteredComponent);
        try {
            final boolean noLock = service.criticalSection(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    ClusteredComponent clusteredComponent = em.readByKey(ClusteredComponent.class, "1_repairable_module");
                    final LockResult<ClusteredComponent> lock = em.lock(LockType.SKIP_LOCKED, clusteredComponent);

                    return lock.getLocked().isEmpty();
                }
            }, 1000l, TimeUnit.MILLISECONDS);

        } finally {
            tm.commit();
        }
    }


}
