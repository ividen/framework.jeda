package ru.kwanza.jeda.context.dictionary.dbinteractor;

public class MSSQLDictionaryDbInteractorTest extends AbstractJDBCDictionaryDbInteractorTest {

    @Override
    protected String getContextFileName() {
        return "mssql-dictionary-db-interactor-test-config.xml";
    }

}
