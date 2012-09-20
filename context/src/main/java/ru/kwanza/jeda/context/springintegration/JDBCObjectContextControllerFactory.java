package ru.kwanza.jeda.context.springintegration;

import ru.kwanza.jeda.api.IContextController;
import ru.kwanza.jeda.context.ObjectContext;
import ru.kwanza.jeda.context.jdbc.JDBCObjectContextController;

import java.util.Map;

/**
 * @author Dmitry Zagorovsky
 */
class JDBCObjectContextControllerFactory extends AbstractJDBCContextControllerFactory {

    private Class<? extends ObjectContext> clazz;
    private Map<String, String> tableColumnByPropertyName;

    public IContextController getObject() throws Exception {
        @SuppressWarnings("unchecked")
        JDBCObjectContextController controller = new JDBCObjectContextController(clazz, tableColumnByPropertyName);

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
        return JDBCObjectContextController.class;
    }

    public void setClazz(Class<? extends ObjectContext> clazz) {
        this.clazz = clazz;
    }

    public void setTableColumnByPropertyName(Map<String, String> tableColumnByPropertyName) {
        this.tableColumnByPropertyName = tableColumnByPropertyName;
    }

}
