package ru.kwanza.jeda.context.springintegration;

import ru.kwanza.jeda.api.IContextController;
import ru.kwanza.jeda.context.dictionary.dbinteractor.JDBCDictionaryDbInteractor;
import ru.kwanza.jeda.context.jdbc.JDBCBlobContextControllerWithDictionary;

/**
 * @author Dmitry Zagorovsky
 */
class JDBCBlobContextControllerWithDictFactory extends AbstractJDBCContextControllerFactory {

    private JDBCDictionaryDbInteractor dbInteractor;
    private String dictionaryTableName;
    private String dictionaryPropertyColumnName;
    private String dictionaryIdColumnName;

    public IContextController getObject() throws Exception {
        JDBCBlobContextControllerWithDictionary controller =
                new JDBCBlobContextControllerWithDictionary(dbInteractor, dictionaryTableName, dictionaryPropertyColumnName, dictionaryIdColumnName);

        controller.setDbTool(dbTool);
        controller.setVersionGenerator(versionGenerator);

        if (terminator != null) {
            controller.setTerminator(terminator);
        }

        if (tableName != null) {
            controller.setTableName(tableName);
        }

        if (idColumnName != null) {
            controller.setIdColumnName(idColumnName);
        }

        if (versionColumnName != null) {
            controller.setVersionColumnName(versionColumnName);
        }

        if (terminatorColumnName != null) {
            controller.setTerminatorColumnName(terminatorColumnName);
        }

        return manager.registerContextController(name, controller);
    }

    public Class<?> getObjectType() {
        return JDBCBlobContextControllerWithDictionary.class;
    }

    public void setDbInteractor(JDBCDictionaryDbInteractor dbInteractor) {
        this.dbInteractor = dbInteractor;
    }

    public void setDictionaryTableName(String dictionaryTableName) {
        this.dictionaryTableName = dictionaryTableName;
    }

    public void setDictionaryPropertyColumnName(String dictionaryPropertyColumnName) {
        this.dictionaryPropertyColumnName = dictionaryPropertyColumnName;
    }

    public void setDictionaryIdColumnName(String dictionaryIdColumnName) {
        this.dictionaryIdColumnName = dictionaryIdColumnName;
    }

}

