package ru.kwanza.jeda.core.pendingstore;

public class TestMSSQLDefaultPendingStore extends AbstractDefaultPendingStoreTest {

    @Override
    protected String getContextFileName() {
        return "mssql-env-context.xml";
    }

}
