package ru.kwanza.jeda.timer;

/**
 * @autor Sergey Shurinov 05.03.12 18:07
 */
public class TestOracleJDBCController extends TestJDBCController {
    @Override
    protected String getContextFileName() {
        return "persistencetimer-oracle-test-config.xml";
    }
}
