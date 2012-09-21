package ru.kwanza.jeda.context.springintegration;

import ru.kwanza.jeda.api.IContextController;
import ru.kwanza.jeda.context.berkeley.BerkeleyBlobContextControllerWithDictionary;
import ru.kwanza.jeda.context.dictionary.dbinteractor.BerkeleyDictionaryDbInteractor;

class BerkeleyBlobContextControllerWithDictFactory extends AbstractBerkeleyBlobContextControllerFactory {

    private BerkeleyDictionaryDbInteractor dbInteractor;
    private String dictionaryTableName;

    public IContextController getObject() throws Exception {
        BerkeleyBlobContextControllerWithDictionary controller = new BerkeleyBlobContextControllerWithDictionary(dbInteractor, dictionaryTableName);
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
        return BerkeleyBlobContextControllerWithDictionary.class;
    }

    public void setDbInteractor(BerkeleyDictionaryDbInteractor dbInteractor) {
        this.dbInteractor = dbInteractor;
    }

    public void setDictionaryTableName(String dictionaryTableName) {
        this.dictionaryTableName = dictionaryTableName;
    }

}
