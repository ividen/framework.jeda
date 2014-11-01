package ru.kwanza.jeda.persistentqueue.db;

import org.dbunit.IDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author Alexander Guzanov
 */
public class InitDB {
    private final String initialDataSet;
    @Resource(name = "dbTester")
    private IDatabaseTester dbTester;

    public InitDB(String initialDataSet) {
        this.initialDataSet = initialDataSet;
    }

    private IDataSet getDataSet() throws Exception {
        return new FlatXmlDataSetBuilder()
                .build(this.getClass().getResourceAsStream(initialDataSet));
    }

    @PostConstruct
    protected void init() throws Exception {
        dbTester.setDataSet(getDataSet());
        dbTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        dbTester.onSetup();
    }
}
