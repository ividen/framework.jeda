package ru.kwanza.jeda.context.springintegration;

import ru.kwanza.jeda.api.IContextController;
import ru.kwanza.jeda.context.jdbc.JDBCBlobContextController;

class JDBCBlobContextControllerFactory extends AbstractJDBCContextControllerFactory {

    public IContextController getObject() throws Exception {
        JDBCBlobContextController controller = new JDBCBlobContextController();
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
        return JDBCBlobContextController.class;
    }

}
