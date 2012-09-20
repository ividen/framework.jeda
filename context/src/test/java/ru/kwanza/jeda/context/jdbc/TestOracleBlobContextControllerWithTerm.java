package ru.kwanza.jeda.context.jdbc;

/**
 * @author Dmitry Zagorovsky
 */
public class TestOracleBlobContextControllerWithTerm extends AbstractJDBCBlobContextControllerWithTermTest {

    public TestOracleBlobContextControllerWithTerm() throws Exception {
        super();
    }

    @Override
    protected String getContextFileName() {
        return "oracle-blob-context-controller-test-config.xml";
    }

}