package ru.kwanza.jeda.core.pendingstore;

/**
 * @author Dmitry Zagorovsky
 */
public class TestMSSQLDefaultPendingStore extends AbstractDefaultPendingStoreTest {

    @Override
    protected String getContextFileName() {
        return "mssql-env-context.xml";
    }

}
