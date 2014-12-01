package ru.kwanza.jeda.context.dictionary.dbinteractor;

import ru.kwanza.jeda.context.dictionary.ContextDictionaryController;
import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;

public abstract class AbstractJDBCDictionaryDbControllerTest extends AbstractDictionaryDbControllerTest {

    @Override
    protected String getDbInteractorBeanName() {
        return JDBCDictionaryDbController.class.getName();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Connection conn = ctx.getBean("dataSource", BasicDataSource.class).getConnection();
        conn.prepareStatement("DELETE FROM " + DICTIONARY_TABLE_NAME).execute();
        dictionaryController = new ContextDictionaryController(dbInteractor, DICTIONARY_TABLE_NAME, DICTIONARY_ID_COLUMN, DICTIONARY_NAME_COLUMN);
    }

    @Override
    protected String getReadNullNameExceptionMessage() {
        return "Incorrect result size: expected 1, actual 0";
    }

}
