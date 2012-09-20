package ru.kwanza.jeda.core.pendingstore;

import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.SortedTable;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Dmitry Zagorovsky
 */
public class DBUnitUtil {

    private DatabaseConnection connection;

    public DBUnitUtil(Connection connection) throws DatabaseUnitException, SQLException {
        if ("oracle".equalsIgnoreCase(connection.getMetaData().getDatabaseProductName())) {
            this.connection = new DatabaseConnection(connection, connection.getMetaData().getUserName());
        } else {
            this.connection = new DatabaseConnection(connection);
        }
    }

    public void assertDBTable(String tableName, String expectedXmlResName) throws Exception {
        printExpectedXml(tableName);
        IDataSet dataSet = getExpectedDataSet(expectedXmlResName);
        ITable expectedTable = new SortedTable(dataSet.getTable(tableName));
        ITable actualTable = new SortedTable(getActualTableData(tableName));
        Assertion.assertEquals(expectedTable, actualTable);
    }

    public int getRowCount(String tableName) throws SQLException {
        return connection.getRowCount(tableName);
    }

    private IDataSet getExpectedDataSet(String expectedXmlResName) throws Exception {
        return new FlatXmlDataSetBuilder().build(this.getClass().getResourceAsStream(expectedXmlResName));
    }

    protected ITable getActualTableData(String tableName) throws Exception {
        return connection.createDataSet(new String[]{tableName}).getTable(tableName);
    }

    /**
     * Используется для печати ожидаемого xml
     *
     * @param tableNames массив имен табличек, xml-слепок которых нужно напечатать
     * @throws Exception ex
     */
    @SuppressWarnings("UnusedDeclaration")
    private void printExpectedXml(String... tableNames) throws Exception {
        QueryDataSet partialDataSet = new QueryDataSet(connection);
        for (String table : tableNames) {
            partialDataSet.addTable(table);
        }
        StringWriter writer = new StringWriter();
        FlatXmlDataSet.write(partialDataSet, writer);
        System.out.println("Expected xml: " + writer);
    }

}
