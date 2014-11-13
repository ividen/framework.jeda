package ru.kwanza.jeda.clusterservice.impl.db;

import mockit.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.impl.db.orm.ComponentEntity;
import ru.kwanza.jeda.clusterservice.impl.db.orm.NodeEntity;

import java.util.Arrays;
import java.util.Collections;

import static junit.framework.Assert.*;

/**
 * @author Alexander Guzanov
 */
public class TestDBClusterService {

    @Tested
    private DBClusterService service;
    @Injectable
    private DBClusterServiceDao dao;
    @Injectable
    private ComponentRepository repository;
    @Injectable
    private WorkerController workers;
    @Mocked
    private NodeEntity currentNode;
    @Mocked
    private ApplicationContext context;


    @Before()
    public void setUp() {

        new NonStrictExpectations() {{
            currentNode.getId();
            result = 1;
            currentNode.getIpAddress();
            result = "1.1.1.1";
            currentNode.getPid();
            result = "pid";
            dao.findOrCreateNode((ru.kwanza.jeda.clusterservice.impl.db.orm.NodeEntity) any);
            result = currentNode;
        }};
    }

    private void start() {
        service.setActivityInterval(10);
        service.setCurrentNodeId(1);
        service.setFailoverInterval(100);
        service.init();
        service.onApplicationEvent(new ContextRefreshedEvent(context));
    }


    @Test
    public void testParams() throws InterruptedException {
        start();
        assertEquals(1, service.getCurrentNodeId().intValue());
        assertEquals(1, service.getCurrentNodeId().intValue());
        assertEquals(100, service.getFailoverInterval());
        service.destroy();
    }

    @Test
    public void testDelegate() throws InterruptedException {
        start();
        assertEquals(currentNode, service.getCurrentNode());
        assertEquals(1, service.getCurrentNodeId().intValue());
        new Expectations() {{
            dao.selectActiveNodes();
            result = Arrays.asList(currentNode);
            dao.selectPassiveNodes();
            result = Collections.emptyList();
            dao.selectNodes();
            result = Arrays.asList(currentNode);
        }};
        assertEquals(Arrays.asList(currentNode), service.getNodes());
        assertEquals(Arrays.asList(currentNode), service.getActiveNodes());
        assertTrue(service.getPassiveNodes().isEmpty());

        service.destroy();
    }


    @Test
    public void testRegisterComponents(@Mocked final IClusteredComponent c1,
                                       @Mocked final IClusteredComponent c2,
                                       @Mocked final IClusteredComponent c3) throws InterruptedException {


        service.registerComponent(c1);
        service.registerComponent(c2);
        service.registerComponent(c3);

        new VerificationsInOrder() {{
            dao.findOrCreateComponent(currentNode, c1);
            times = 1;
            repository.registerComponent(c1);
            dao.findOrCreateComponent(currentNode, c2);
            times = 1;
            repository.registerComponent(c2);
            dao.findOrCreateComponent(currentNode, c3);
            times = 1;
            repository.registerComponent(c3);
        }};

    }

    @Test
    public void testRegisterComponentsAfterStart(@Mocked final IClusteredComponent c1,
                                                 @Mocked final IClusteredComponent c2,
                                                 @Mocked final IClusteredComponent c3) throws InterruptedException {


        service.registerComponent(c1);
        service.registerComponent(c2);
        start();
        try {
            service.registerComponent(c3);
            fail("ExpectedException");
        } catch (IllegalStateException e) {
        }

        new VerificationsInOrder() {{
            dao.findOrCreateComponent(currentNode, c1);
            times = 1;
            repository.registerComponent(c1);
            dao.findOrCreateComponent(currentNode, c2);
            times = 1;
            repository.registerComponent(c2);
        }};

        service.destroy();
    }


    @Test
    public void testProcessInitialStates_1(@Mocked final IClusteredComponent c1,
                                           @Mocked final IClusteredComponent c2,
                                           @Mocked final ComponentEntity e_1,
                                           @Mocked final ComponentEntity e_2) throws InterruptedException {


        new Expectations() {{
        }};


        service.run();


        new VerificationsInOrder() {{
            dao.findOrCreateComponent(currentNode, c1);
            times = 1;
            repository.registerComponent(c1);
            dao.findOrCreateComponent(currentNode, c2);
            times = 1;
            repository.registerComponent(c2);
        }};

        service.destroy();
    }


}
