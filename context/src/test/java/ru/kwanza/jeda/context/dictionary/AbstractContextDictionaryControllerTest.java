package ru.kwanza.jeda.context.dictionary;

import ru.kwanza.autokey.api.IAutoKey;
import ru.kwanza.autokey.mock.MockAutoKeyImpl;
import ru.kwanza.dbtool.DBTool;
import ru.kwanza.jeda.context.DBUnitUtil;
import ru.kwanza.jeda.context.dictionary.dbinteractor.DictionaryDbInteractor;
import ru.kwanza.jeda.context.dictionary.dbinteractor.JDBCDictionaryDbInteractor;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Dmitry Zagorovsky
 */
public abstract class AbstractContextDictionaryControllerTest extends TestCase {

    protected static final String[] PROP_NAMES = new String[]{"prop1", "prop2", "prop3", "prop4", "prop5"};
    protected static final String DICTIONARY_TABLE_NAME = "ctx_dictionary";

    protected ApplicationContext ctx;
    protected Connection conn;
    protected DBUnitUtil dbUnitUtil;

    DBTool dbTool;
    DictionaryDbInteractor dbInteractor;

    protected ContextDictionaryController dictionaryController;

    protected abstract String getContextFileName();

    @Override
    public void setUp() throws Exception {
        executeServiceQuery("DELETE FROM " + DICTIONARY_TABLE_NAME);
        ctx = new ClassPathXmlApplicationContext(getContextFileName(), this.getClass());
        conn = ctx.getBean("dataSource", BasicDataSource.class).getConnection();
        dbUnitUtil = new DBUnitUtil(conn);

        dbTool = ctx.getBean("dbtool.DBTool", DBTool.class);

        IAutoKey autoKey = ctx.getBean("autokey.IAutoKey", IAutoKey.class);
        ((MockAutoKeyImpl) autoKey).resetSequences();

        dbInteractor = ctx.getBean("ru.kwanza.jeda.context.dictionary.dbinteractor.JDBCDictionaryDbInteractor", JDBCDictionaryDbInteractor.class);
        dictionaryController = new ContextDictionaryController(dbInteractor, "ctx_dictionary", "name", "id");
    }

    @Override
    public void tearDown() throws Exception {
        ((ClassPathXmlApplicationContext) ctx).close();
    }

    public void testSimpleGetNameAndId() throws Exception {
        Assert.assertEquals(getTestIdByName(), dictionaryController.getPropertyIds(getTestPropNames()));
        Assert.assertEquals(getTestNameById(), dictionaryController.getPropertyNames(getTestIds()));

        dbUnitUtil.assertDBTable("ctx_dictionary", "dictionary/testSimpleNameStore.xml");
    }

    public void testGetNameAndIdWithTwoAlreadyInserted() throws Exception {
        insertDictionaryRecord("prop1", 123l);
        insertDictionaryRecord("prop3", 157l);

        // Names.
        Map<String, Long> actualIdByName = dictionaryController.getPropertyIds(getTestPropNames());
        assertEquals(5, actualIdByName.size());
        assertEquals(Long.valueOf("123"), actualIdByName.get("prop1"));
        // Ids.
        Map<Long, String> actualNameByID = dictionaryController.getPropertyNames(Arrays.asList(123l, 157l));
        assertEquals(2, actualNameByID.size());
        assertEquals("prop1", actualNameByID.get(123l));
        assertEquals("prop3", actualNameByID.get(157l));
        dbUnitUtil.assertDBTable("ctx_dictionary", "dictionary/testNameStoreWithTwoAlreadyInserted.xml");
    }

    public void testGetNameAndIdWithAllAlreadyInserted() throws Exception {
        Map<String, Long> expectedIdByName = new HashMap<String, Long>();
        Map<Long, String> expectedNameById = new HashMap<Long, String>();
        List<Long> testIds = new ArrayList<Long>();
        long startId = 123;
        for (String name : PROP_NAMES) {
            long testId = startId++;
            insertDictionaryRecord(name, testId);
            expectedIdByName.put(name, testId);
            expectedNameById.put(testId, name);
            testIds.add(testId);
        }

        Assert.assertEquals(expectedIdByName, dictionaryController.getPropertyIds(getTestPropNames()));
        Assert.assertEquals(expectedNameById, dictionaryController.getPropertyNames(testIds));
    }

    public void testCacheInit() throws Exception {
        Map<String, Long> idByName = insertTestMapIntoDb();
        ContextDictionaryController ctrl = new ContextDictionaryController(dbInteractor,  "ctx_dictionary", "name", "id");
        Assert.assertEquals(idByName, ctrl.getDictionaryCache().getPropIdByName());
    }

    public void testCachePopulationWhenGetPropIds() throws Exception {
        insertTestMapIntoDb();
        dictionaryController.getPropertyIds(getTestPropNames().subList(1, 3));
        assertCachePopulation();
    }

    public void testCachePopulationWhenGetPropNames() throws Exception {
        insertTestMapIntoDb();
        dictionaryController.getPropertyNames(getTestIds().subList(1, 3));
        assertCachePopulation();
    }


    public void testGetPropertyNmeWhenItAbsent() throws Exception {
        insertTestMapIntoDb();
        PreparedStatement pst = conn.prepareStatement("DELETE FROM " + DICTIONARY_TABLE_NAME + " where id = ?");
        pst.setLong(1, 2l);
        pst.execute();

        try {
            dictionaryController.getPropertyNames(getTestIds().subList(1, 3));
            fail("Runtime exception must be thrown");
        } catch (Exception e) {
            assertEquals("Incorrect result size: expected 1, actual 0", e.getMessage());
        }
    }

    private void assertCachePopulation() {
        Map<String, Long> expectedIdByName = new HashMap<String, Long>() {
        };
        expectedIdByName.put(PROP_NAMES[1], 2l);
        expectedIdByName.put(PROP_NAMES[2], 3l);

        Map<Long, String> expectedNameById = new HashMap<Long, String>();
        expectedNameById.put(2l, PROP_NAMES[1]);
        expectedNameById.put(3l, PROP_NAMES[2]);

        Map<String, Long> actualIdByName = dictionaryController.getDictionaryCache().getPropIdByName();
        Map<Long, String> actualNameById = dictionaryController.getDictionaryCache().getPropNameById();

        assertEquals(2, actualIdByName.size());
        assertEquals(2, actualNameById.size());
        assertEquals(expectedIdByName, actualIdByName);
        assertEquals(expectedNameById, actualNameById);
    }

    private Map<String, Long> insertTestMapIntoDb() throws Exception {
        Map<String, Long> idByName = getTestIdByName();
        for (Map.Entry<String, Long> entry : idByName.entrySet()) {
            insertDictionaryRecord(entry.getKey(), entry.getValue());
        }
        return idByName;
    }

    private void insertDictionaryRecord(String propName, long id) throws Exception {
        PreparedStatement pst = conn.prepareStatement("INSERT INTO " + DICTIONARY_TABLE_NAME + " (name, id) VALUES(?, ?)");
        pst.setString(1, propName);
        pst.setLong(2, id);
        assertEquals("No update count!", false, pst.execute());
        assertEquals("Wrong update count!", 1, pst.getUpdateCount());
    }

    private List<String> getTestPropNames() {
        return Arrays.asList(PROP_NAMES);
    }

    private List<Long> getTestIds() {
        return Arrays.asList(1l, 2l, 3l, 4l, 5l);
    }

    private Map<String, Long> getTestIdByName() {
        Map<String, Long> idByName = new HashMap<String, Long>();
        idByName.put(PROP_NAMES[0], 1l);
        idByName.put(PROP_NAMES[1], 2l);
        idByName.put(PROP_NAMES[2], 3l);
        idByName.put(PROP_NAMES[3], 4l);
        idByName.put(PROP_NAMES[4], 5l);
        return idByName;
    }

    private Map<Long, String> getTestNameById() {
        Map<Long, String> nameById = new HashMap<Long, String>();
        nameById.put(1l, PROP_NAMES[0]);
        nameById.put(2l, PROP_NAMES[1]);
        nameById.put(3l, PROP_NAMES[2]);
        nameById.put(4l, PROP_NAMES[3]);
        nameById.put(5l, PROP_NAMES[4]);

        return nameById;
    }

    public void executeServiceQuery(String sql) throws SQLException {
        ApplicationContext srvCtx = new ClassPathXmlApplicationContext(getContextFileName(), this.getClass());
        Connection conn = srvCtx.getBean("dataSource", BasicDataSource.class).getConnection();
        conn.prepareStatement(sql).execute();
        ((ClassPathXmlApplicationContext) srvCtx).close();
    }

}
