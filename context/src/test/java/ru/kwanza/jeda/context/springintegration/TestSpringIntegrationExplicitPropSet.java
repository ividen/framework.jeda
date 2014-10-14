package ru.kwanza.jeda.context.springintegration;

import ru.kwanza.jeda.api.IJedaManager;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestSpringIntegrationExplicitPropSet extends TestCase {

    private IJedaManager manager;

    private static final String JDBC_BLOB_CONTEXT_CONTROLLER_BEAN = "ru.kwanza.jeda.context.springintegration.JDBCBlobContextControllerFactory#0";
    private static final String JDBC_BLOB_CONTEXT_CONTROLLER_WITH_DICT_BEAN = "ru.kwanza.jeda.context.springintegration.JDBCBlobContextControllerWithDictFactory#0";
    private static final String JDBC_OBJECT_CONTEXT_CONTROLLER_BEAN = "ru.kwanza.jeda.context.springintegration.JDBCObjectContextControllerFactory#0";
    private static final String BERKELEY_BLOB_CONTEXT_CONTROLLER_BEAN = "ru.kwanza.jeda.context.springintegration.BerkeleyBlobContextControllerFactory#0";
    private static final String BERKELEY_BLOB_CONTEXT_CONTROLLER_WITH_DICT_BEAN = "ru.kwanza.jeda.context.springintegration.BerkeleyBlobContextControllerWithDictFactory#0";

    @Override
    public void setUp() throws Exception {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-integration-explicit-prop-set-test.xml", TestSpringIntegrationExplicitPropSet.class);
        manager = (IJedaManager) ctx.getBean(IJedaManager.class.getName());
    }

    public void testSmoke() throws Exception {
        Assert.assertNotNull(manager.getContextController(JDBC_BLOB_CONTEXT_CONTROLLER_BEAN));
        Assert.assertNotNull(manager.getContextController(JDBC_BLOB_CONTEXT_CONTROLLER_WITH_DICT_BEAN));
        Assert.assertNotNull(manager.getContextController(JDBC_OBJECT_CONTEXT_CONTROLLER_BEAN));
        Assert.assertNotNull(manager.getContextController(BERKELEY_BLOB_CONTEXT_CONTROLLER_BEAN));
        Assert.assertNotNull(manager.getContextController(BERKELEY_BLOB_CONTEXT_CONTROLLER_WITH_DICT_BEAN));
    }

//    <jeda-context:jdbc-blob-context-controller
//            tableName="table_name" terminator="termName" idColumnName="id" versionColumnName="version"
//    terminatorColumnName="terminator" dbTool="dbtool.DBTool" versionGenerator="dbtool.VersionGenerator"/>
//    public void testJDBCBlobContextControllerSpringInt() throws Exception {
//        JDBCBlobContextController ctxController = (JDBCBlobContextController)manager.getContextController(JDBC_BLOB_CONTEXT_CONTROLLER_BEAN);
//        Assert.assertEquals("table_name", ctxController.getTableName());
//        Assert.assertEquals("termName", ctxController.getTerminator());
//        Assert.assertEquals("id", ctxController.getIdColumnName());
//        Assert.assertEquals("version", ctxController.getVersionColumnName());
//        Assert.assertEquals("terminator", ctxController.getTerminatorColumnName());
//        Assert.assertNotNull(ctxController.getDbTool());
//        Assert.assertNotNull(ctxController.getVersionGenerator());
//    }

}
