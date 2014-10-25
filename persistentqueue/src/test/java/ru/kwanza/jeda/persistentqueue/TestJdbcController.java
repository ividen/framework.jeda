package ru.kwanza.jeda.persistentqueue;

/**
 * @author Ivan Baluk
 */
public class TestJdbcController extends AbstractTestController {
    @Override
    protected String getContextFileName() {
        return "persistencequeue-jdbc-test-config.xml";
    }

}
