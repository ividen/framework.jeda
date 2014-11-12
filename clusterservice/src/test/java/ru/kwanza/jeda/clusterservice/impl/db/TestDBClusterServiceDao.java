package ru.kwanza.jeda.clusterservice.impl.db;

import junit.framework.Assert;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import org.dbunit.Assertion;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.SortedDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import ru.kwanza.dbtool.core.DBTool;
import ru.kwanza.dbtool.core.UpdateException;
import ru.kwanza.dbtool.orm.api.IEntityManager;
import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.impl.db.orm.ComponentEntity;
import ru.kwanza.jeda.clusterservice.impl.db.orm.NodeEntity;

import javax.annotation.Resource;

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
    @Autowired
    private DBClusterServiceDao dao;


    protected IDataSet getActualDataSet(String tablename) throws Exception {
        return new SortedDataSet(new DatabaseConnection(dbTool.getJDBCConnection())
                .createDataSet(new String[]{tablename}));
    }

    protected IDataSet getResourceSet(String fileName) throws DataSetException {
        return new SortedDataSet(new FlatXmlDataSetBuilder().build(this.getClass().getResourceAsStream(fileName)));
    }


    @Test
    public void testRegister_1(@Mocked final NodeEntity n,
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
                getResourceSet("data_set_1.xml"), "jeda_clustered_component", new String[]{"version", "hold_node_id", "last_activity"});
    }

    @Test
    public void testRegister_2(@Mocked final NodeEntity n,
                               @Mocked final IClusteredComponent c) throws Exception {
        new Expectations() {{
            n.getId();
            result = 1;
            c.getName();
            result = "test_component";
        }};

        em.create(new ComponentEntity(n.getId(),c.getName()));

        final ComponentEntity entity = dao.findOrCreateComponent(n, c);
        Assert.assertEquals("1_test_component", entity.getId());
        Assert.assertEquals(1, entity.getNodeId().intValue());
        Assert.assertEquals("test_component", entity.getName());

        Assertion.assertEqualsIgnoreCols(getActualDataSet("jeda_clustered_component"),
                getResourceSet("data_set_1.xml"), "jeda_clustered_component", new String[]{"version", "hold_node_id", "last_activity"});
    }



    @Tested
    private DBClusterServiceDao mockedDao;
    @Injectable
    private IEntityManager mockedEm;


    @Test
    public void testRegister_3(@Mocked final NodeEntity n,
                               @Mocked final IClusteredComponent c
                               ) throws Exception {
        new Expectations() {{
            n.getId();result = 1;
            c.getName(); result = "test_component";
            mockedEm.readByKey(ComponentEntity.class,any);result=null;
            mockedEm.create(any); result = new UpdateException();
            mockedEm.readByKey(ComponentEntity.class,any);result=null;
        }};


        try {
            mockedDao.findOrCreateComponent(n, c);
            Assert.fail("Expected " + RuntimeException.class);
        }catch (RuntimeException e){
        }


    }

}
