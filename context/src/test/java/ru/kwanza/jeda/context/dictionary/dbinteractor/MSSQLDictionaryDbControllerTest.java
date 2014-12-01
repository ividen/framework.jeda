package ru.kwanza.jeda.context.dictionary.dbinteractor;

public class MSSQLDictionaryDbControllerTest extends AbstractJDBCDictionaryDbControllerTest {

    @Override
    protected String getContextFileName() {
        return "mssql-dictionary-db-interactor-test-config.xml";
    }

}
