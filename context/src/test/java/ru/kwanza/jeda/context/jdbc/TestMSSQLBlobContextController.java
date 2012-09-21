package ru.kwanza.jeda.context.jdbc;

public class TestMSSQLBlobContextController extends AbstractJDBCBlobContextControllerTest {

    @Override
    protected String getContextFileName() {
        return "mssql-blob-context-controller-test-config.xml";
    }

}
