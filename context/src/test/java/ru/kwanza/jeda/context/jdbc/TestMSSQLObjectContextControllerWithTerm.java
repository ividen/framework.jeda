package ru.kwanza.jeda.context.jdbc;

/**
 * @author Dmitry Zagorovsky
 */
public class TestMSSQLObjectContextControllerWithTerm extends AbstractObjectContextControllerWithTermTest {

    @Override
    protected String getContextFileName() {
        return "mssql-object-context-controller-test-config.xml";
    }

}
