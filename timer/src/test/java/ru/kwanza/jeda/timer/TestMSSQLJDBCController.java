package ru.kwanza.jeda.timer;

public class TestMSSQLJDBCController extends TestJDBCController {
    @Override
    protected String getContextFileName() {
        return "persistencetimer-mssql-test-config.xml";
    }
}
