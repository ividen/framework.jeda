package ru.kwanza.jeda.context.jdbc;

public class TestMSSQLBlobContextWithDictionary extends AbstractJDBCBlobContextControllerTest {

    protected static final String DICTIONARY_TABLE_NAME = "ctx_dictionary";

    public TestMSSQLBlobContextWithDictionary() throws Exception {
        super();
    }

    @Override
    public void setUp() throws Exception {
        executeServiceQuery("DELETE FROM " + DICTIONARY_TABLE_NAME);
        super.setUp();
        ctxController = ctx.getBean("jdbcBlobContextControllerWithDict", JDBCBlobContextControllerWithDictionary.class);
    }

    @Override
    protected String getContextFileName() {
        return "mssql-blob-context-controller-test-config.xml";
    }

    public String getDbUnitResourcePostfix() {
        return "WithDict";
    }

}
