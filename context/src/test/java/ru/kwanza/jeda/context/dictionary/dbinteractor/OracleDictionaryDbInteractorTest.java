package ru.kwanza.jeda.context.dictionary.dbinteractor;

public class OracleDictionaryDbInteractorTest extends AbstractJDBCDictionaryDbInteractorTest {

    @Override
    protected String getContextFileName() {
        return "oracle-dictionary-db-interactor-test-config.xml";
    }

}
