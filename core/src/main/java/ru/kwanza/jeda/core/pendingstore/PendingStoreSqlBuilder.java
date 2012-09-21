package ru.kwanza.jeda.core.pendingstore;

public class PendingStoreSqlBuilder {

    private static final String INSERT_SQL = "INSERT INTO %s (%s, %s, %s, %s) VALUES(?, ?, ?, ?)";
    private static final String SELECT_SQL = "SELECT %s, %s, %s, %s FROM %s WHERE %s IN(?)";
    private static final String DELETE_SQL = "DELETE FROM %s where %s = ?";

    private DefaultPendingStore pendingStore;

    private String insertSql = null;
    private String selectSql = null;
    private String deleteSql = null;

    public PendingStoreSqlBuilder(DefaultPendingStore pendingStore) {
        this.pendingStore = pendingStore;
    }

    public String getInsertSql() {
        if (insertSql == null) {
            insertSql = buildInsertSql();
        }
        return insertSql;
    }

    public String getSelectSql() {
        if (selectSql == null) {
            selectSql = buildSelectSql();
        }
        return selectSql;
    }

    public String getDeleteSql() {
        if (deleteSql == null) {
            deleteSql = buildDeleteSql();
        }
        return deleteSql;
    }

    private String buildInsertSql() {
        return String.format(INSERT_SQL,
                pendingStore.getTableName(),
                pendingStore.getIdColumnName(),
                pendingStore.getSinkNameColumnName(),
                pendingStore.getEventDescriptionColumnName(),
                pendingStore.getEventDataColumnName());
    }

    private String buildSelectSql() {
        return String.format(SELECT_SQL,
                pendingStore.getIdColumnName(),
                pendingStore.getSinkNameColumnName(),
                pendingStore.getEventDescriptionColumnName(),
                pendingStore.getEventDataColumnName(),
                pendingStore.getTableName(),
                pendingStore.getIdColumnName());
    }

    private String buildDeleteSql() {
        return String.format(DELETE_SQL,
                pendingStore.getTableName(),
                pendingStore.getIdColumnName());
    }

}
