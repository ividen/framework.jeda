package ru.kwanza.jeda.clusterservice.impl.db;

import junit.framework.Assert;
import mockit.*;
import org.dbunit.Assertion;
import org.dbunit.IDatabaseTester;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.SortedDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import ru.kwanza.dbtool.core.DBTool;
import ru.kwanza.dbtool.core.UpdateException;
import ru.kwanza.dbtool.orm.api.IEntityManager;
import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.jeda.clusterservice.impl.db.orm.AlienComponent;
import ru.kwanza.jeda.clusterservice.impl.db.orm.ComponentEntity;
import ru.kwanza.jeda.clusterservice.impl.db.orm.NodeEntity;
import ru.kwanza.jeda.clusterservice.impl.db.orm.WaitForReturnComponent;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Alexander Guzanov
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(locations = "application-config.xml")
public class TestDBClusterServiceDao extends AbstractTransactionalJUnit4SpringContextTests {
    @Autowired
    private DBTool dbTool;
    @Resource(name = "dbtool.IEntityManager")
    private IEntityManager em;
    @Resource(name = "dbTester")
    private IDatabaseTester dbTester;
    @Autowired
    private DBClusterServiceDao dao;
    @Tested
    private DBClusterServiceDao mockedDao;
    @Injectable
    private IEntityManager mockedEm;

    protected IDataSet getActualDataSet(String tablename) throws Exception {
        return new SortedDataSet(new DatabaseConnection(dbTool.getJDBCConnection())
                .createDataSet(new String[]{tablename}));
    }

    protected IDataSet getResourceSet(String fileName) throws DataSetException {
        return new SortedDataSet(new FlatXmlDataSetBuilder().build(this.getClass().getResourceAsStream(fileName)));
    }

    private IDataSet getDataSet(String fileName) throws Exception {
        return new FlatXmlDataSetBuilder()
                .build(this.getClass().getResourceAsStream(fileName));
    }

    private void initDataSet(String fileName) throws Exception {
        dbTester.setDataSet(getDataSet(fileName));
        dbTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        dbTester.onSetup();
    }

    @Test
    public void testRegisterComponent_1(@Mocked final NodeEntity n,
                                        @Mocked final IClusteredComponent c) throws Exception {
        new Expectations() {{
            n.getId();
            result = 1;
            c.getName();
            result = "test_component";
        }};
        final ComponentEntity entity = dao.findOrCreateComponent(n, c);
        Assert.assertEquals("1_test_component", entity.getId());
        Assert.assertEquals(1, entity.getNodeId().intValue());
        Assert.assertEquals("test_component", entity.getName());

        Assertion.assertEqualsIgnoreCols(getActualDataSet("jeda_clustered_component"),
                getResourceSet("data_set_1.xml"), "jeda_clustered_component", new String[]{"version", "wait_for_return",
                        "hold_node_id", "last_activity"});
    }

    @Test
    public void testRegisterComponent_2(@Mocked final NodeEntity n,
                                        @Mocked final IClusteredComponent c) throws Exception {
        new Expectations() {{
            n.getId();
            result = 1;
            c.getName();
            result = "test_component";
        }};

        em.create(new ComponentEntity(n.getId(), c.getName()));

        final ComponentEntity entity = dao.findOrCreateComponent(n, c);
        Assert.assertEquals("1_test_component", entity.getId());
        Assert.assertEquals(1, entity.getNodeId().intValue());
        Assert.assertEquals("test_component", entity.getName());

        Assertion.assertEqualsIgnoreCols(getActualDataSet("jeda_clustered_component"),
                getResourceSet("data_set_1.xml"), "jeda_clustered_component", new String[]{"version","wait_for_return", "hold_node_id", "last_activity"});
    }


    @Test(expected = RuntimeException.class)
    public void testRegisterComponent_3(@Mocked final NodeEntity n,
                                        @Mocked final IClusteredComponent c
    ) throws Exception {
        new Expectations() {{
            n.getId();
            result = 1;
            c.getName();
            result = "test_component";
            mockedEm.readByKey(ComponentEntity.class, any);
            result = null;
            mockedEm.create(any);
            result = new UpdateException();
            mockedEm.readByKey(ComponentEntity.class, any);
            result = null;
        }};


        mockedDao.findOrCreateComponent(n, c);
        Assert.fail("Expected " + RuntimeException.class);
    }

    @Test
    public void testRegisterNode_1(@Mocked final NodeEntity n) throws Exception {
        new Expectations() {{
            n.getId();
            result = 1;
            n.getLastActivity();
            result = 666;
            n.getIpAddress();
            result = "1.1.1.1";
            n.getPid();
            result = "test_pid";
        }};
        NodeEntity result = dao.findOrCreateNode(n);
        Assert.assertEquals(1, result.getId().intValue());
        Assert.assertEquals("1.1.1.1", result.getIpAddress());
        Assert.assertEquals(666, result.getLastActivity().intValue());
        Assert.assertEquals("test_pid", result.getPid());

        Assertion.assertEquals(getActualDataSet("jeda_cluster_node"),
                getResourceSet("data_set_2.xml"));
    }

    @Test
    public void testRegisterNode_2(@Mocked final NodeEntity n) throws Exception {
        new Expectations() {{
            n.getId();
            result = 1;
            n.getLastActivity();
            result = 666;
            n.getIpAddress();
            result = "1.1.1.1";
            n.getPid();
            result = "test_pid";
        }};

        em.create(n);

        NodeEntity result = dao.findOrCreateNode(n);
        Assert.assertEquals(1, result.getId().intValue());
        Assert.assertEquals("1.1.1.1", result.getIpAddress());
        Assert.assertEquals(666, result.getLastActivity().intValue());
        Assert.assertEquals("test_pid", result.getPid());

        Assertion.assertEquals(getActualDataSet("jeda_cluster_node"),
                getResourceSet("data_set_2.xml"));
    }

    @Test(expected = RuntimeException.class)
    public void testRegisterNode_3(@Mocked final NodeEntity n) throws Exception {
        new Expectations() {{
            n.getId();
            result = 1;
            mockedEm.readByKey(NodeEntity.class, any);
            result = null;
            mockedEm.create(n);
            result = new UpdateException();
            mockedEm.readByKey(NodeEntity.class, any);
            result = null;
        }};

        NodeEntity result = mockedDao.findOrCreateNode(n);
    }

    @Test
    public void testLoadComponentsByKeys_1() {
        Assert.assertTrue(dao.loadComponentsByKey(Collections.<String>emptyList()).isEmpty());
        Assert.assertTrue(dao.loadComponentsByKey(Arrays.asList("1_cp_1", "1_cp_2", "1_cp_3")).isEmpty());
    }

    @Test
    public void testLoadComponentsByKeys_2() throws Exception {
        initDataSet("init_data_set_2.xml");
        Assert.assertTrue(dao.loadComponentsByKey(Collections.<String>emptyList()).isEmpty());
        Assert.assertTrue(dao.loadComponentsByKey(Arrays.asList("cp_1", "cp_2", "cp_3")).isEmpty());

        Collection<ComponentEntity> result = dao.loadComponentsByKey(Arrays.asList("1_test_component"));
        Assert.assertEquals(1, result.size());
        ComponentEntity entity = result.iterator().next();

        Assert.assertEquals("1_test_component", entity.getId());
        Assert.assertEquals(1, entity.getNodeId().intValue());
        Assert.assertEquals("test_component", entity.getName());

        Assert.assertEquals(1, entity.getNode().getId().intValue());
        Assert.assertEquals("1.1.1.1", entity.getNode().getIpAddress());
        Assert.assertEquals(666, entity.getNode().getLastActivity().intValue());
        Assert.assertEquals("test_pid", entity.getNode().getPid());
    }

    @Test
    public void testSelectActive(@Mocked({"currentTimeMillis"}) final System system) throws Exception {
        initDataSet("init_data_set_3.xml");

        new Expectations() {{
            System.currentTimeMillis();
            result = 0l;
        }};

        List<? extends Node> nodeEntities = dao.selectActiveNodes();
        Assert.assertEquals(2, nodeEntities.size());
        Assert.assertEquals(1, nodeEntities.get(0).getId().intValue());
        Assert.assertEquals("1.1.1.1", nodeEntities.get(0).getIpAddress());
        Assert.assertEquals(2, nodeEntities.get(1).getId().intValue());
        Assert.assertEquals("1.1.1.2", nodeEntities.get(1).getIpAddress());

        new Expectations() {{
            System.currentTimeMillis();
            result = 80l;
        }};

        nodeEntities = dao.selectActiveNodes();
        Assert.assertEquals(1, nodeEntities.size());
        Assert.assertEquals(2, nodeEntities.get(0).getId().intValue());
        Assert.assertEquals("1.1.1.2", nodeEntities.get(0).getIpAddress());

        new Expectations() {{
            System.currentTimeMillis();
            result = 200l;
        }};
        nodeEntities = dao.selectActiveNodes();
        Assert.assertEquals(0, nodeEntities.size());
    }

    @Test
    public void testSelectPassive(@Mocked({"currentTimeMillis"}) final System system) throws Exception {
        new Expectations() {{
            System.currentTimeMillis();
            result = 200l;
        }};

        initDataSet("init_data_set_3.xml");

        new Expectations() {{
            System.currentTimeMillis();
            result = 200l;
        }};

        List<? extends Node> nodeEntities = dao.selectPassiveNodes();
        Assert.assertEquals(2, nodeEntities.size());
        Assert.assertEquals(1, nodeEntities.get(0).getId().intValue());
        Assert.assertEquals("1.1.1.1", nodeEntities.get(0).getIpAddress());
        Assert.assertEquals(2, nodeEntities.get(1).getId().intValue());
        Assert.assertEquals("1.1.1.2", nodeEntities.get(1).getIpAddress());

        new Expectations() {{
            System.currentTimeMillis();
            result = 80l;
        }};

        nodeEntities = dao.selectPassiveNodes();
        Assert.assertEquals(1, nodeEntities.size());
        Assert.assertEquals(1, nodeEntities.get(0).getId().intValue());
        Assert.assertEquals("1.1.1.1", nodeEntities.get(0).getIpAddress());

        new Expectations() {{
            System.currentTimeMillis();
            result = 0l;
        }};
        nodeEntities = dao.selectPassiveNodes();
        Assert.assertEquals(0, nodeEntities.size());
    }

    @Test
    public void testSelectAll() throws Exception {
        initDataSet("init_data_set_3.xml");

        List<? extends Node> nodeEntities = dao.selectNodes();
        Assert.assertEquals(2, nodeEntities.size());
        Assert.assertEquals(1, nodeEntities.get(0).getId().intValue());
        Assert.assertEquals("1.1.1.1", nodeEntities.get(0).getIpAddress());
        Assert.assertEquals(2, nodeEntities.get(1).getId().intValue());
        Assert.assertEquals("1.1.1.2", nodeEntities.get(1).getIpAddress());
    }


    @Test
    public void testSelectComponents(@Mocked final Node node) throws Exception {
        initDataSet("init_data_set_2.xml");

        new Expectations() {{
            node.getId();
            result = 1;
        }};

        Collection<ComponentEntity> result = dao.selectComponents(node, Arrays.asList("test_component"));
        Assert.assertEquals(1, result.size());
        ComponentEntity entity = result.iterator().next();

        Assert.assertEquals("1_test_component", entity.getId());
        Assert.assertEquals(1, entity.getNodeId().intValue());
        Assert.assertEquals("test_component", entity.getName());

        Assert.assertEquals(1, entity.getNode().getId().intValue());
        Assert.assertEquals("1.1.1.1", entity.getNode().getIpAddress());
        Assert.assertEquals(666, entity.getNode().getLastActivity().intValue());
        Assert.assertEquals("test_pid", entity.getNode().getPid());
    }


    @Test
    public void testSelectAlienStale_1(@Mocked final Node node,
                                     @Mocked({"currentTimeMillis"}) final System system) throws Exception {
        initDataSet("init_data_set_4.xml");

        new NonStrictExpectations() {{
            node.getId();
            result = 2;
        }};


        List<ComponentEntity> componentEntities = dao.selectAlienStaleComponents(node, Arrays.asList("test_component_1", "test_component_2"), 200l);
        Assert.assertEquals(2, componentEntities.size());
        Assert.assertEquals("1_test_component_1", componentEntities.get(0).getId());
        Assert.assertEquals("1_test_component_2", componentEntities.get(1).getId());


        componentEntities = dao.selectAlienStaleComponents(node, Arrays.asList("test_component_1", "test_component_2"), 80l);
        Assert.assertEquals(1, componentEntities.size());
        Assert.assertEquals("1_test_component_1", componentEntities.get(0).getId());

        componentEntities = dao.selectAlienStaleComponents(node, Arrays.asList("test_component_1", "test_component_2"), 0l);
        Assert.assertEquals(0, componentEntities.size());
    }


    @Test
    public void testSelectAlienStale_2(@Mocked final Node node,
                                     @Mocked({"currentTimeMillis"}) final System system) throws Exception {
        initDataSet("init_data_set_5.xml");

        new NonStrictExpectations() {{
            node.getId();
            result = 2;
        }};


        List<ComponentEntity> componentEntities = dao.selectAlienStaleComponents(node, Arrays.asList("test_component_1", "test_component_2"), 200l);
        Assert.assertEquals(1, componentEntities.size());
        Assert.assertEquals("1_test_component_1", componentEntities.get(0).getId());


        componentEntities = dao.selectAlienStaleComponents(node, Arrays.asList("test_component_1", "test_component_2"), 80l);
        Assert.assertEquals(1, componentEntities.size());
        Assert.assertEquals("1_test_component_1", componentEntities.get(0).getId());

        componentEntities = dao.selectAlienStaleComponents(node, Arrays.asList("test_component_1", "test_component_2"), 0l);
        Assert.assertEquals(0, componentEntities.size());
    }


    @Test
    public void testUpdateNode_1() throws Exception {
        initDataSet("init_data_set_6.xml");

        NodeEntity n = new NodeEntity(1,777l,"2.2.2.2","new_pid");

        dao.updateNode(n);

        Assertion.assertEquals(getActualDataSet("jeda_cluster_node"),
                getResourceSet("data_set_5.xml"));
    }


    @Test
    public void testUpdateNode_6() throws Exception {
        initDataSet("init_data_set_6.xml");

        NodeEntity n = new NodeEntity(1,777l,"2.2.2.2","new_pid");

        new Expectations(){{
            mockedEm.update(any); result=new UpdateException();times=1;
        }};

        mockedDao.updateNode(n);

        Assertion.assertEquals(getActualDataSet("jeda_cluster_node"),
                getResourceSet("init_data_set_6.xml"));
    }


    @Test
    public void testUpdateComponent() throws Exception {
        initDataSet("init_data_set_4.xml");

        Collection<ComponentEntity> componentEntities =
                dao.loadComponentsByKey(Arrays.asList("1_test_component_1", "1_test_component_2",
                        "2_test_component_1", "2_test_component_2"));

        for (ComponentEntity componentEntity : componentEntities) {
            componentEntity.setLastActivity(-666l);
        }

        dao.updateComponents(componentEntities);

        Assertion.assertEqualsIgnoreCols(getActualDataSet("jeda_clustered_component"),
                getResourceSet("data_set_3.xml"), "jeda_clustered_component", new String[]{"version", "wait_for_return",
                        "hold_node_id"});
    }



    @Test
    public void testUpdateAlien() throws Exception {
        initDataSet("init_data_set_4.xml");

        Collection<ComponentEntity> componentEntities =
                dao.loadComponentsByKey(Arrays.asList("1_test_component_1", "1_test_component_2",
                        "2_test_component_1", "2_test_component_2"));

        List<AlienComponent> alienComponent = new ArrayList<AlienComponent>();
        for (ComponentEntity componentEntity : componentEntities) {
            componentEntity.setLastActivity(-666l);
            alienComponent.add(new AlienComponent(componentEntity));
        }

        dao.updateAlienComponents(alienComponent);

        Assertion.assertEqualsIgnoreCols(getActualDataSet("jeda_clustered_component"),
                getResourceSet("data_set_3.xml"), "jeda_clustered_component", new String[]{"version", "wait_for_return",
                        "hold_node_id"});
    }


    @Test
    public void testMarkWaiteReturn_1() throws Exception {
        initDataSet("init_data_set_4.xml");

        Collection<ComponentEntity> componentEntities =
                dao.loadComponentsByKey(Arrays.asList("1_test_component_1", "1_test_component_2",
                        "2_test_component_1", "2_test_component_2"));

        for (ComponentEntity componentEntity : componentEntities) {
            componentEntity.setLastActivity(-666l);
        }

        dao.markWaitForReturn(componentEntities, 777l);

        Assertion.assertEqualsIgnoreCols(getActualDataSet("jeda_clustered_component"),
                getResourceSet("data_set_4.xml"), "jeda_clustered_component", new String[]{"hold_node_id"});
    }


    @Test
    public void testMarkWaiteReturn_2() throws Exception {
        initDataSet("init_data_set_4.xml");

        Collection<ComponentEntity> componentEntities =
                dao.loadComponentsByKey(Arrays.asList("1_test_component_1", "1_test_component_2",
                        "2_test_component_1", "2_test_component_2"));

        for (ComponentEntity componentEntity : componentEntities) {
            componentEntity.setLastActivity(-666l);
        }

        new Expectations() {{
            mockedEm.update(WaitForReturnComponent.class, (Collection) any);
            result = new UpdateException();
        }};

        mockedDao.markWaitForReturn(componentEntities, 6666);

        Assertion.assertEqualsIgnoreCols(getActualDataSet("jeda_clustered_component"),
                getResourceSet("init_data_set_4.xml"), "jeda_clustered_component", new String[]{"hold_node_id","wait_for_return"});
    }

    @Test
    public void testSelectActivationCandidate(@Mocked final Node node) throws Exception {
        initDataSet("init_data_set_4.xml");

        new NonStrictExpectations(){{
           node.getId();result  = 1;
        }};

        Collection<ComponentEntity> componentEntities = dao.selectActivationCandidate(Arrays.asList("1_test_component_1", "1_test_component_2"), 200l);
        Assert.assertEquals(2, componentEntities.size());
        final Iterator<ComponentEntity> iterator = componentEntities.iterator();
        Assert.assertEquals("1_test_component_1", iterator.next().getId());
        Assert.assertEquals("1_test_component_2", iterator.next().getId());


        componentEntities = dao.selectActivationCandidate(Arrays.asList("2_test_component_1", "2_test_component_2"), 80l);
        Assert.assertEquals(1, componentEntities.size());
        Assert.assertEquals("2_test_component_1", componentEntities.iterator().next().getId());

        componentEntities = dao.selectActivationCandidate(Arrays.asList("1_test_component_1",
                "1_test_component_2","2_test_component_1","2_test_component_2"), 0l);
        Assert.assertEquals(0, componentEntities.size());
    }

}
