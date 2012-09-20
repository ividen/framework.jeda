package ru.kwanza.jeda.context.springintegration;

import ru.kwanza.jeda.api.IContextController;
import ru.kwanza.jeda.context.berkeley.BerkeleyBlobContextController;

/**
 * @author Dmitry Zagorovsky
 */
public class BerkeleyBlobContextControllerFactory extends AbstractBerkeleyBlobContextControllerFactory {

    public IContextController getObject() throws Exception {
        BerkeleyBlobContextController controller = new BerkeleyBlobContextController();
        controller.setVersionGenerator(versionGenerator);

        if (databaseName != null) {
            controller.setDatabaseName(databaseName);
        }

        if (connectionFactory != null) {
            controller.setConnectionFactory(connectionFactory);
        }

        if (versionGenerator != null) {
            controller.setVersionGenerator(versionGenerator);
        }

        if (terminator != null) {
            controller.setTerminator(terminator);
        }

        return manager.registerContextController(name, controller);
    }

    public Class<?> getObjectType() {
        return BerkeleyBlobContextController.class;
    }



}
