package ru.kwanza.jeda.context.jdbc;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Dmitry Zagorovsky
 */
public class JDBCContextSqlBuilder {

    private static final String SELECT_SQL = "SELECT %s, %s, %s FROM %s WHERE %s IN(?)";
    private static final String INSERT_SQL = "INSERT INTO %s (%s, %s, %s) VALUES (?, ?, %s)";
    private static final String UPDATE_SQL = "UPDATE %s set %s = ?, %s where %s = ? AND %s = ?";
    private static final String DELETE_SQL = "DELETE FROM %s WHERE %s = ?";
    private static final String CHECK_UPDATE_SQL = "SELECT %s, %s FROM %s WHERE %s IN(?)";

    private static final String SELECT_SQL_WITH_TERMINATOR = "SELECT %s, %s, %s, %s FROM %s WHERE %s IN(?) AND %s = ?";
    private static final String INSERT_SQL_WITH_TERMINATOR = "INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, %s)";
    private static final String UPDATE_SQL_WITH_TERMINATOR = "UPDATE %s set %s = ?, %s where %s = ? AND %s = ? AND %s = ?";
    private static final String DELETE_SQL_WITH_TERMINATOR = "DELETE FROM %s WHERE %s = ? AND %s = ?";
    private static final String CHECK_UPDATE_SQL_WITH_TERMINATOR = "SELECT %s, %s FROM %s WHERE %s IN(?) AND %s = '%s'";

    private AbstractJDBCContextController controller;
    Collection<String> additionalColumns;

    private String selectSql = null;
    private String insertSql = null;
    private String updateSql = null;
    private String deleteSql = null;
    private String checkUpdateSql = null;

    public JDBCContextSqlBuilder(JDBCBlobContextController controller) {
        this(controller, Arrays.asList(controller.getContextDataColumnName()));
    }

    public JDBCContextSqlBuilder(AbstractJDBCContextController controller, Collection<String> additionalColumns) {
        this.controller = controller;
        this.additionalColumns = additionalColumns;
    }

    public String getSelectSql() {
        if (selectSql == null) {
            selectSql = buildSelectSql();
        }
        return selectSql;
    }

    public String getInsertSql() {
        if (insertSql == null) {
            insertSql = buildInsertSql();
        }
        return insertSql;
    }

    public String getUpdateSql() {
        if (updateSql == null) {
            updateSql = buildUpdateSql();
        }
        return updateSql;
    }

    public String getDeleteSql() {
        if (deleteSql == null) {
            deleteSql = buildDeleteSql();
        }
        return deleteSql;
    }

    public String getCheckUpdateSql() {
        if (checkUpdateSql == null) {
            checkUpdateSql = buildCheckUpdateSql();
        }
        return checkUpdateSql;
    }

    private String buildSelectSql() {
        if (controller.terminator == null) {
            return String.format(SELECT_SQL,
                    controller.getIdColumnName(),
                    controller.getVersionColumnName(),
                    getSelectAndInsertSqlColumnExtension(),
                    controller.getTableName(),
                    controller.getIdColumnName());
        } else {
            return String.format(SELECT_SQL_WITH_TERMINATOR,
                    controller.getIdColumnName(),
                    controller.getVersionColumnName(),
                    controller.getTerminatorColumnName(),
                    getSelectAndInsertSqlColumnExtension(),
                    controller.getTableName(),
                    controller.getIdColumnName(),
                    controller.getTerminatorColumnName());
        }
    }

    private String buildInsertSql() {
        if (controller.getTerminator() == null) {
            return String.format(INSERT_SQL,
                    controller.getTableName(),
                    controller.getIdColumnName(),
                    controller.getVersionColumnName(),
                    getSelectAndInsertSqlColumnExtension(),
                    getInsertSqlValueExtension());
        } else {
            return String.format(INSERT_SQL_WITH_TERMINATOR,
                    controller.getTableName(),
                    controller.getIdColumnName(),
                    controller.getVersionColumnName(),
                    controller.getTerminatorColumnName(),
                    getSelectAndInsertSqlColumnExtension(),
                    getInsertSqlValueExtension());
        }
    }

    private String buildUpdateSql() {
        if (controller.terminator == null) {
            return String.format(UPDATE_SQL,
                    controller.getTableName(),
                    controller.getVersionColumnName(),
                    getUpdateSqlColumnExtension(),
                    controller.getIdColumnName(),
                    controller.getVersionColumnName());
        } else {
            return String.format(UPDATE_SQL_WITH_TERMINATOR,
                    controller.getTableName(),
                    controller.getVersionColumnName(),
                    getUpdateSqlColumnExtension(),
                    controller.getIdColumnName(),
                    controller.getVersionColumnName(),
                    controller.getTerminatorColumnName());
        }
    }

    private String buildDeleteSql() {
        if (controller.terminator == null) {
            return String.format(DELETE_SQL,
                    controller.getTableName(),
                    controller.getIdColumnName());
        } else {
            return String.format(DELETE_SQL_WITH_TERMINATOR,
                    controller.getTableName(),
                    controller.getIdColumnName(),
                    controller.getTerminatorColumnName());
        }
    }

    private String buildCheckUpdateSql() {
        if (controller.terminator == null) {
            return String.format(CHECK_UPDATE_SQL,
                    controller.getIdColumnName(),
                    controller.getVersionColumnName(),
                    controller.getTableName(),
                    controller.getIdColumnName());
        } else {
            return String.format(CHECK_UPDATE_SQL_WITH_TERMINATOR,
                    controller.getIdColumnName(),
                    controller.getVersionColumnName(),
                    controller.getTableName(),
                    controller.getIdColumnName(),
                    controller.getTerminatorColumnName(),
                    controller.getTerminator());
        }
    }

    private String getSelectAndInsertSqlColumnExtension() {
        String ext = null;
        for (String column : additionalColumns) {
            if (ext == null) {
                ext = "";
            } else {
                ext = ext + ", ";
            }
            ext = ext + column;
        }
        return ext;
    }

    private String getInsertSqlValueExtension() {
        String ext = "";
        for (int i = 0; i < additionalColumns.size(); i++) {
            if (i != 0) {
                ext = ext + ", ";
            }
            ext = ext + "?";
        }
        return ext;
    }

    private String getUpdateSqlColumnExtension() {
        String ext = null;
        for (String column : additionalColumns) {
            if (ext == null) {
                ext = "";
            } else {
                ext = ext + ", ";
            }
            ext = ext + column + " = ?";
        }
        return ext;
    }

}
