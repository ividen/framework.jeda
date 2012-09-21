package ru.kwanza.jeda.context.dictionary.dbinteractor;

import ru.kwanza.jeda.context.dictionary.ContextDictionaryController;
import junit.framework.Assert;
import junit.framework.TestCase;

public class TestJDBCDictionaryDbInteractorSqlGeneration extends TestCase {

    private ContextDictionaryController ctxDictionary = new ContextDictionaryController();
    private JDBCDictionaryDbInteractor dbInteractor = new JDBCDictionaryDbInteractor();

    public void testInsertSqlFormat() {
        Assert.assertEquals("INSERT INTO ctx_dictionary (name, id) VALUES (?,?)",
                dbInteractor.getInsertSql(ctxDictionary));
    }

    public void testSelectByIdSqlFormat() {
        Assert.assertEquals("SELECT name FROM ctx_dictionary WHERE id = ?",
                dbInteractor.getSelectByIdSql(ctxDictionary));
    }

    public void testSelectByNameSqlFormat() {
        Assert.assertEquals("SELECT id FROM ctx_dictionary WHERE name = ?",
                dbInteractor.getSelectByNameSql(ctxDictionary));
    }

    public void testSelectAllSqlFormat() {
        Assert.assertEquals("SELECT name, id FROM ctx_dictionary",
                dbInteractor.getSelectAllSql(ctxDictionary));
    }

}
