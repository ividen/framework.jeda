package ru.kwanza.jeda.context.jdbc;

/**
 * @author Dmitry Zagorovsky
 */
public class TestOracleObjectContextControllerWithTerm extends AbstractObjectContextControllerWithTermTest {

    @Override
    protected String getContextFileName() {
        return "oracle-object-context-controller-test-config.xml";
    }

}