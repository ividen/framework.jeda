package ru.kwanza.jeda.context.dictionary.dbinteractor;

import ru.kwanza.jeda.context.dictionary.ContextDictionaryController;

import java.io.File;
import java.io.IOException;

/**
 * @author Dmitry Zagorovsky
 */
public class TestBerkeleyDictionaryDbInteractor extends AbstractDictionaryDbInteractorTest {

    @Override
    protected String getContextFileName() {
        return "berkeley-dictionary-db-interactor-test-config.xml";
    }

    @Override
    protected String getDbInteractorBeanName() {
        return BerkeleyDictionaryDbInteractor.class.getName();
    }

    @Override
    public void setUp() throws Exception {
        clean();
        super.setUp();
        dictionaryController = new ContextDictionaryController(dbInteractor, DICTIONARY_TABLE_NAME, null, null);
    }

    @Override
    protected String getReadNullNameExceptionMessage() {
        return "Property with id '1240' not found";
    }

    protected void clean() throws Exception {
        delete(new File("./target/test_berkeley_db"));
    }

    private void delete(File file) throws IOException {
        if (file.isDirectory()) {
            for (File item : file.listFiles()) {
                delete(item);
            }
        }
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

}
