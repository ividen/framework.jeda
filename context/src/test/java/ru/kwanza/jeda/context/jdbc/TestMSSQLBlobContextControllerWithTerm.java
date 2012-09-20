package ru.kwanza.jeda.context.jdbc;

/**
 * @author Dmitry Zagorovsky
 */
public class TestMSSQLBlobContextControllerWithTerm extends AbstractJDBCBlobContextControllerWithTermTest {

    public TestMSSQLBlobContextControllerWithTerm() throws Exception {
        super();
    }

    @Override
    protected String getContextFileName() {
        return "mssql-blob-context-controller-test-config.xml";
    }

}
