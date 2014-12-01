package ru.kwanza.jeda.context.dictionary.dbinteractor;

public class OracleDictionaryDbControllerTest extends AbstractJDBCDictionaryDbControllerTest {

    @Override
    protected String getContextFileName() {
        return "oracle-dictionary-db-interactor-test-config.xml";
    }

}
