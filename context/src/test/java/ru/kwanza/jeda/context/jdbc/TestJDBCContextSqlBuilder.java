package ru.kwanza.jeda.context.jdbc;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.Arrays;

public class TestJDBCContextSqlBuilder extends TestCase {

    private JDBCContextSqlBuilder sqlBuilder;
    private JDBCContextSqlBuilder sqlBuilderWithTerm;

    private JDBCContextSqlBuilder varSqlBuilder;
    private JDBCContextSqlBuilder varSqlBuilderWithTerm;

    @Override
    public void setUp() throws Exception {
        sqlBuilder = new JDBCContextSqlBuilder(getCtxController());
        sqlBuilderWithTerm = new JDBCContextSqlBuilder(getCtxControllerWithTerminator());

        varSqlBuilder = new JDBCContextSqlBuilder(getCtxController(), Arrays.asList("prop_1", "prop_2", "prop_3"));
        varSqlBuilderWithTerm = new JDBCContextSqlBuilder(getCtxControllerWithTerminator(), Arrays.asList("prop_1", "prop_2", "prop_3"));
    }

    public void testSelectSqlFormat() {
        Assert.assertEquals("SELECT id_c, version_c, blob_c FROM context_t WHERE id_c IN(?)",
                sqlBuilder.getSelectSql());

        Assert.assertEquals("SELECT id_c, version_c, terminator_c, blob_c FROM context_t WHERE id_c IN(?) AND terminator_c = ?",
                sqlBuilderWithTerm.getSelectSql());

        Assert.assertEquals("SELECT id_c, version_c, prop_1, prop_2, prop_3 FROM context_t WHERE id_c IN(?)",
                varSqlBuilder.getSelectSql());
        Assert.assertEquals("SELECT id_c, version_c, terminator_c, prop_1, prop_2, prop_3 FROM context_t WHERE id_c IN(?) AND terminator_c = ?",
                varSqlBuilderWithTerm.getSelectSql());
    }

    public void testInsertSqlFormat() {
        Assert.assertEquals("INSERT INTO context_t (id_c, version_c, blob_c) VALUES (?, ?, ?)",
                sqlBuilder.getInsertSql());
        Assert.assertEquals("INSERT INTO context_t (id_c, version_c, terminator_c, blob_c) VALUES (?, ?, ?, ?)",
                sqlBuilderWithTerm.getInsertSql());

        Assert.assertEquals("INSERT INTO context_t (id_c, version_c, prop_1, prop_2, prop_3) VALUES (?, ?, ?, ?, ?)",
                varSqlBuilder.getInsertSql());
        Assert.assertEquals("INSERT INTO context_t (id_c, version_c, terminator_c, prop_1, prop_2, prop_3) VALUES (?, ?, ?, ?, ?, ?)",
                varSqlBuilderWithTerm.getInsertSql());
    }

    public void testUpdateSqlFormat() {
        Assert.assertEquals("UPDATE context_t set version_c = ?, blob_c = ? where id_c = ? AND version_c = ?",
                sqlBuilder.getUpdateSql());
        Assert.assertEquals("UPDATE context_t set version_c = ?, blob_c = ? where id_c = ? AND version_c = ? AND terminator_c = ?",
                sqlBuilderWithTerm.getUpdateSql());

        Assert.assertEquals("UPDATE context_t set version_c = ?, prop_1 = ?, prop_2 = ?, prop_3 = ? where id_c = ? AND version_c = ?",
                varSqlBuilder.getUpdateSql());
        Assert.assertEquals("UPDATE context_t set version_c = ?, prop_1 = ?, prop_2 = ?, prop_3 = ? where id_c = ? AND version_c = ? AND terminator_c = ?",
                varSqlBuilderWithTerm.getUpdateSql());
    }

    public void testDeleteSqlFormat() {
        Assert.assertEquals("DELETE FROM context_t WHERE id_c = ?",
                sqlBuilder.getDeleteSql());
        Assert.assertEquals("DELETE FROM context_t WHERE id_c = ? AND terminator_c = ?",
                sqlBuilderWithTerm.getDeleteSql());

        Assert.assertEquals("DELETE FROM context_t WHERE id_c = ?",
                varSqlBuilder.getDeleteSql());
        Assert.assertEquals("DELETE FROM context_t WHERE id_c = ? AND terminator_c = ?",
                varSqlBuilderWithTerm.getDeleteSql());
    }

    public void testCheckUpdateSqlFormat() {
        Assert.assertEquals("SELECT id_c, version_c FROM context_t WHERE id_c IN(?)",
                sqlBuilder.getCheckUpdateSql());
        Assert.assertEquals("SELECT id_c, version_c FROM context_t WHERE id_c IN(?) AND terminator_c = 'terminatorName'",
                sqlBuilderWithTerm.getCheckUpdateSql());

        Assert.assertEquals("SELECT id_c, version_c FROM context_t WHERE id_c IN(?)",
                varSqlBuilder.getCheckUpdateSql());
        Assert.assertEquals("SELECT id_c, version_c FROM context_t WHERE id_c IN(?) AND terminator_c = 'terminatorName'",
                varSqlBuilderWithTerm.getCheckUpdateSql());
    }

    private static JDBCBlobContextController getCtxController() {
        JDBCBlobContextController contextController = new JDBCBlobContextController();
        contextController.setTableName("context_t");
        contextController.setIdColumnName("id_c");
        contextController.setVersionColumnName("version_c");
        contextController.setContextDataColumnName("blob_c");
        return contextController;
    }

    private static JDBCBlobContextController getCtxControllerWithTerminator() {
        JDBCBlobContextController contextController = getCtxController();
        contextController.setTerminatorColumnName("terminator_c");
        contextController.setTerminator("terminatorName");
        return contextController;
    }

}
