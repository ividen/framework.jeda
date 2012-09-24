package ru.kwanza.jeda.timer;


public class TestOracleJDBCController extends TestJDBCController {
    @Override
    protected String getContextFileName() {
        return "persistencetimer-oracle-test-config.xml";
    }
}
