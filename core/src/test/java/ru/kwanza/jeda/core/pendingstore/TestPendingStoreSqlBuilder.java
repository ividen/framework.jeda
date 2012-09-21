package ru.kwanza.jeda.core.pendingstore;

import junit.framework.Assert;
import junit.framework.TestCase;

public class TestPendingStoreSqlBuilder extends TestCase {

    private PendingStoreSqlBuilder sqlBuilder;

    @Override
    public void setUp() throws Exception {
        sqlBuilder = new PendingStoreSqlBuilder(getPendingStore());
    }

    public void testInsertSqlBuild() throws Exception {
        Assert.assertEquals("INSERT INTO pending_store_table (id_col, sink_name_col, evt_descr_col, evt_blob) VALUES(?, ?, ?, ?)",
                sqlBuilder.getInsertSql());
    }

    public void testSelectSqlBuild() throws Exception {
        Assert.assertEquals("SELECT id_col, sink_name_col, evt_descr_col, evt_blob FROM pending_store_table WHERE id_col IN(?)",
                sqlBuilder.getSelectSql());
    }

    public void testDeleteSqlBuild() {
        Assert.assertEquals("DELETE FROM pending_store_table where id_col = ?", sqlBuilder.getDeleteSql());
    }

    private static DefaultPendingStore getPendingStore() {
        DefaultPendingStore pendingStore = new DefaultPendingStore(null, null, null);
        pendingStore.setTableName("pending_store_table");
        pendingStore.setIdColumnName("id_col");
        pendingStore.setSinkNameColumnName("sink_name_col");
        pendingStore.setEventDescriptionColumnName("evt_descr_col");
        pendingStore.setEventDataColumnName("evt_blob");
        return pendingStore;
    }

}
