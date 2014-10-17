package ru.kwanza.jeda.clusterservice.db;

import junit.framework.Assert;
import org.dbunit.Assertion;
import org.dbunit.IDatabaseTester;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.SortedDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import ru.kwanza.dbtool.core.DBTool;
import ru.kwanza.jeda.clusterservice.IClusterService;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author Alexander Guzanov
 */
@ContextConfiguration(locations = "application-config.xml")
public class TestDBClusterService extends AbstractJUnit4SpringContextTests {
    @Resource(name = "dbtool.DBTool")
    private DBTool dbTool;

    @Resource(name = "jeda.clusterservice.DBClusterService")
    private IClusterService service;

    @Component
    public static class InitDB {
        @Resource(name = "dbTester")
        private IDatabaseTester dbTester;

        private IDataSet getDataSet() throws Exception {
            return new FlatXmlDataSetBuilder()
                    .build(this.getClass().getResourceAsStream("init_data_set.xml"));
        }

        @PostConstruct
        protected void init() throws Exception {
            dbTester.setDataSet(getDataSet());
            dbTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
            dbTester.onSetup();
        }
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
            service.registerModule(new TestModule("module_4"));
            Assert.fail("Expected " + IllegalStateException.class);
        } catch (IllegalStateException e) {

        }
        Assertion.assertEqualsIgnoreCols(getResourceSet("data_set_2.xml"),
                getActualDataSet("jeda_clustered_module"), "jeda_clustered_module", new String[]{"last_repaired"});
    }


    protected IDataSet getActualDataSet(String tablename) throws Exception {
        return new SortedDataSet(new DatabaseConnection(dbTool.getJDBCConnection())
                .createDataSet(new String[]{tablename}));
    }

    protected IDataSet getResourceSet(String fileName) throws DataSetException {
        return new SortedDataSet(new FlatXmlDataSetBuilder().build(this.getClass().getResourceAsStream(fileName)));
    }


}
