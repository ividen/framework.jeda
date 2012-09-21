package ru.kwanza.jeda.context.jdbc;

public class TestOracleBlobContextWithDictionary extends AbstractJDBCBlobContextControllerTest {

    protected static final String DICTIONARY_TABLE_NAME = "ctx_dictionary";

    @Override
    public void setUp() throws Exception {
        executeServiceQuery("DELETE FROM " + DICTIONARY_TABLE_NAME);
        super.setUp();
        ctxController = ctx.getBean("jdbcBlobContextControllerWithDict", JDBCBlobContextControllerWithDictionary.class);
    }

    @Override
    protected String getContextFileName() {
        return "oracle-blob-context-controller-test-config.xml";
    }

    public String getDbUnitResourcePostfix() {
        return "WithDict";
    }

}
