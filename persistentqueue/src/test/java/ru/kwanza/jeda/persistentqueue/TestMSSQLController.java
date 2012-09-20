package ru.kwanza.jeda.persistentqueue;

/**
 * @author Ivan Baluk
 */
public class TestMSSQLController extends AbstractTestController {
    @Override
    protected String getContextFileName() {
        return "persistencequeue-mssql-test-config.xml";
    }
}
