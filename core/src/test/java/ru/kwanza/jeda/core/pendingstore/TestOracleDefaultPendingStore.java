package ru.kwanza.jeda.core.pendingstore;

public class TestOracleDefaultPendingStore extends AbstractDefaultPendingStoreTest {

    @Override
    protected String getContextFileName() {
        return "oracle-env-context.xml";
    }

}
