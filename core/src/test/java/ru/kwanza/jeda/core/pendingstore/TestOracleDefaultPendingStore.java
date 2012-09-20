package ru.kwanza.jeda.core.pendingstore;

/**
 * @author Dmitry Zagorovsky
 */
public class TestOracleDefaultPendingStore extends AbstractDefaultPendingStoreTest {

    @Override
    protected String getContextFileName() {
        return "oracle-env-context.xml";
    }

}
