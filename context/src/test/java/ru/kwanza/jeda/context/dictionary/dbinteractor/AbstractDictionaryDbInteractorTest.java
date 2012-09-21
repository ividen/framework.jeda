package ru.kwanza.jeda.context.dictionary.dbinteractor;

import ru.kwanza.jeda.context.dictionary.ContextDictionaryController;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractDictionaryDbInteractorTest extends TestCase {

    protected static final String[] PROPERTY_NAMES = new String[]{"prop1", "prop2", "prop3"};
    protected static final String DICTIONARY_TABLE_NAME = "ctx_dictionary";
    protected static final String DICTIONARY_ID_COLUMN = "name";
    protected static final String DICTIONARY_NAME_COLUMN = "id";

    protected ApplicationContext ctx;
    protected DictionaryDbInteractor dbInteractor;
    protected ContextDictionaryController dictionaryController;

    protected abstract String getContextFileName();
    protected abstract String getDbInteractorBeanName();

    @Override
    public void setUp() throws Exception {
        ctx = new ClassPathXmlApplicationContext(getContextFileName(), this.getClass());
        dbInteractor = ctx.getBean(getDbInteractorBeanName(), DictionaryDbInteractor.class);
    }

    @Override
    public void tearDown() throws Exception {
        ((ClassPathXmlApplicationContext) ctx).close();
    }

    public void testSimpleStoreNewProperty() throws Exception {
        dbInteractor.storeNewProperty(PROPERTY_NAMES[0], dictionaryController);
        assertIdByPropertyMap(dbInteractor.readAllDictionary(dictionaryController), PROPERTY_NAMES[0]);

        dbInteractor.storeNewProperty(PROPERTY_NAMES[2], dictionaryController);
        assertIdByPropertyMap(dbInteractor.readAllDictionary(dictionaryController), PROPERTY_NAMES[0], PROPERTY_NAMES[2]);
    }

    public void testStoreDuplicateProperty() throws Exception {
        dbInteractor.storeNewProperty(PROPERTY_NAMES[0], dictionaryController);
        dbInteractor.storeNewProperty(PROPERTY_NAMES[0], dictionaryController);
        assertIdByPropertyMap(dbInteractor.readAllDictionary(dictionaryController), PROPERTY_NAMES[0]);
    }

    public void testSimpleReadId() throws Exception {
        dbInteractor.storeNewProperty(PROPERTY_NAMES[0], dictionaryController);
        Assert.assertTrue(dbInteractor.readIdFromDb(PROPERTY_NAMES[0], dictionaryController) != null);
    }

    public void testReadNullId() throws Exception {
        Assert.assertEquals(null, dbInteractor.readIdFromDb(PROPERTY_NAMES[0], dictionaryController));
    }

    public void testSimpleReadName() throws Exception {
        dbInteractor.storeNewProperty(PROPERTY_NAMES[0], dictionaryController);
        Long id = dbInteractor.readIdFromDb(PROPERTY_NAMES[0], dictionaryController);
        Assert.assertEquals(PROPERTY_NAMES[0], dbInteractor.readNameFromDb(id, dictionaryController));
    }

    protected abstract String getReadNullNameExceptionMessage();

    public void testReadNullName() throws Exception {
        try {
            Assert.assertEquals(null, dbInteractor.readNameFromDb(1240l, dictionaryController));
            fail("Runtime Exception must be thrown.");
        } catch (Throwable t) {
            Assert.assertEquals(getReadNullNameExceptionMessage(), t.getMessage());
        }
    }

    private void assertIdByPropertyMap(Map<String, Long> actualIdByPropName, String... expectedPropertyNames) {
        Assert.assertEquals(actualIdByPropName.size(), expectedPropertyNames.length);
        Set<Long> ids = new HashSet<Long>();
        for (String expectedPropName : expectedPropertyNames) {
            Assert.assertTrue(actualIdByPropName.containsKey(expectedPropName));
            ids.add(actualIdByPropName.get(expectedPropName));
        }
        Assert.assertEquals("Probably duplicate ids found...", expectedPropertyNames.length, ids.size());
    }

}
