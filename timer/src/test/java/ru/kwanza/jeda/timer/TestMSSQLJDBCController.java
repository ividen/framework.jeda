package ru.kwanza.jeda.timer;

/**
 * @autor Sergey Shurinov 05.03.12 18:11
 */
public class TestMSSQLJDBCController extends TestJDBCController {
    @Override
    protected String getContextFileName() {
        return "persistencetimer-mssql-test-config.xml";
    }
}
