package ru.kwanza.jeda.context.jdbc;

import ru.kwanza.autokey.api.IAutoKey;
import ru.kwanza.autokey.mock.MockAutoKeyImpl;
import ru.kwanza.jeda.api.ContextStoreException;
import ru.kwanza.jeda.context.DBUnitUtil;
import ru.kwanza.jeda.context.TestObject;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.Connection;
import java.util.*;

/**
 * @author Dmitry Zagorovsky
 */
public abstract class AbstractObjectContextControllerTest extends TestCase {

    protected static final Long[] CONTEXT_IDS = new Long[]{1020l, 1030l, 1040l, 1050l, 1060l};

    protected ApplicationContext ctx;
    protected DBUnitUtil dbUnitUtil;
    protected JDBCObjectContextController<TestObject> ctxController;

    protected abstract String getContextFileName();

    @Override
    public void setUp() throws Exception {
        ctx = new ClassPathXmlApplicationContext(getContextFileName(), this.getClass());
        Connection conn = ctx.getBean("dataSource", BasicDataSource.class).getConnection();
        dbUnitUtil = new DBUnitUtil(conn);

        //noinspection unchecked
        ctxController = ctx.getBean("jdbcObjectContextController", JDBCObjectContextController.class);

        resetAutoKey();
        conn.prepareStatement("DELETE FROM " + getContextTableName()).execute();
    }

    public void testMappingInit() throws Exception {
        Map<String, String> expectedPropertyByColumn = new HashMap<String, String>();
        expectedPropertyByColumn.put("data_1", "data1");
        expectedPropertyByColumn.put("data2", "data2");

        Assert.assertEquals(expectedPropertyByColumn, ctxController.getPropertyByColumn());
    }

    public void testSimpleContextStore() throws Exception {
        List<TestObject> lst = getTestContextList(null);
        ctxController.store(lst);

        Assert.assertEquals(getTestContextList(1l), lst);

        Map<Long, TestObject> actualMap = ctxController.load(Arrays.asList(CONTEXT_IDS));
        Assert.assertEquals(getContextListAsMap(getTestContextList(1l)), actualMap);
    }

    public void testSimpleContextDelete() throws Exception {
        List<TestObject> testContextList = getTestContextList(null);
        ctxController.store(testContextList);
        Assert.assertEquals(CONTEXT_IDS.length, ctxController.load(Arrays.asList(CONTEXT_IDS)).size());

        List<TestObject> listToRemove = Arrays.asList(
                getTestContextList(1l).get(0),
                getTestContextList(1l).get(1));

        ctxController.remove(listToRemove);

        List<TestObject> expectedList = Arrays.asList(
                getTestContextList(1l).get(2),
                getTestContextList(1l).get(3),
                getTestContextList(1l).get(4));

        Map<Long, TestObject> actualMap = ctxController.load(Arrays.asList(CONTEXT_IDS));
        Assert.assertEquals(getContextListAsMap(expectedList), actualMap);
    }

    public void testContextDeleteWithOptimistic() throws Exception {
        List<TestObject> testContextList = getTestContextList(null);
        ctxController.store(testContextList);
        Assert.assertEquals(CONTEXT_IDS.length, ctxController.load(Arrays.asList(CONTEXT_IDS)).size());

        // ???????? ??? ????????. ? ???? ?????? ?????? ?????????? ?? ??.
        List<TestObject> listToRemove = Arrays.asList(
                getTestContextList(2l).get(0),
                getTestContextList(2l).get(1),
                getTestContextList(1l).get(2));

        // ?? ???? ????????? ????? ??????????.
        List<TestObject> optimisticLockList = Arrays.asList(
                getTestContextList(2l).get(0),
                getTestContextList(2l).get(1));

        try {
            ctxController.remove(listToRemove);
            fail("No optimistic lock, but must be!");
        } catch (ContextStoreException e) {
            Assert.assertEquals(optimisticLockList, e.getOptimisticItems());
            Assert.assertNull(e.getOtherFailedItems());
        }

        // ????????, ??????? ?????? ???????? ? ?? ????? ????????.
        List<TestObject> expectedList = Arrays.asList(
                getTestContextList(1l).get(0),
                getTestContextList(1l).get(1),
                getTestContextList(1l).get(3),
                getTestContextList(1l).get(4));


        Map<Long, TestObject> actualMap = ctxController.load(Arrays.asList(CONTEXT_IDS));
        Assert.assertEquals(getContextListAsMap(expectedList), actualMap);
    }

    public void testSimpleContextUpdate() throws Exception {
        List<TestObject> testContextList = getTestContextList(null);
        ctxController.store(testContextList);

        for (TestObject context : testContextList) {
            context.setData1("updated");
        }

        ctxController.store(testContextList);

        Map<Long, TestObject> actualMap = ctxController.load(Arrays.asList(CONTEXT_IDS));

        Assert.assertEquals(getContextListAsMap(testContextList), actualMap);
    }

    public void testContextUpdateWithOptimistic() throws Exception {
        List<TestObject> testContextList = getTestContextList(null);
        ctxController.store(testContextList);

        //
        List<TestObject> updatedCtxList = getTestContextList(1l);
        for (TestObject context : updatedCtxList) {
            context.setData1("updated");
        }
        //
        updatedCtxList.get(0).setVersion(5l);
        updatedCtxList.get(2).setVersion(5l);
        updatedCtxList.get(4).setVersion(5l);

        //
        List<TestObject> lockedItems = Arrays.asList(
                getTestContextList(5l).get(0),
                getTestContextList(5l).get(2),
                getTestContextList(5l).get(4));
        for (TestObject context : lockedItems) {
            context.setData1("updated");
        }

        try {
            ctxController.store(updatedCtxList);
            fail("No optimistic lock, but must be!");
        } catch (ContextStoreException e) {
            Assert.assertEquals(lockedItems, e.getOptimisticItems());
            if (e.getOtherFailedItems() != null) {
                Assert.assertEquals(0, e.getOtherFailedItems().size());

            }
        }

        List<TestObject> expectedCtxList = getTestContextList(1l);
        expectedCtxList.get(1).setData1("updated");
        expectedCtxList.get(3).setData1("updated");

        Map<Long, TestObject> actualMap = ctxController.load(Arrays.asList(CONTEXT_IDS));
        assertContextMapEqualsWithoutVersion(getContextListAsMap(expectedCtxList), actualMap);
    }

    public void testContextInsertConstrained() throws Exception {
        List<TestObject> testContextList = getTestContextList(null);

        testContextList.get(0).setId(null);
        testContextList.get(4).setId(null);

        try {
            ctxController.store(testContextList);
            fail("No constrained, but must be!");
        } catch (ContextStoreException e) {
            Assert.assertEquals(2, e.getOtherFailedItems().size());
            Assert.assertNull(e.getOptimisticItems());
        }
    }

    protected List<TestObject> getTestContextList(Long version) {
        List<TestObject> contextList = new ArrayList<TestObject>();

        for (Long id : CONTEXT_IDS) {
            TestObject to = new TestObject(id, version, "data1" + id, id);
            contextList.add(to);
        }
        return contextList;
    }

    protected Map<Long, TestObject> getContextListAsMap(List<TestObject> contextList) {
        Map<Long, TestObject> contextById = new HashMap<Long, TestObject>();
        for (TestObject context : contextList) {
            contextById.put(context.getId(), context);
        }
        return contextById;
    }

    private void resetAutoKey() {
        IAutoKey autoKey = ctx.getBean("autokey.IAutoKey", IAutoKey.class);
        ((MockAutoKeyImpl) autoKey).resetSequences();
    }

    protected String getContextTableName() {
        return "object_context";
    }

    private void assertContextMapEqualsWithoutVersion(Map<Long, TestObject> expectedMap, Map<Long, TestObject> actualMap) {
        Assert.assertEquals(expectedMap.size(), actualMap.size());
        for (Long key : expectedMap.keySet()) {
            TestObject expectedCtx = expectedMap.get(key);
            TestObject actualCtx = actualMap.get(key);
            Assert.assertNotNull(expectedCtx);
            Assert.assertNotNull(actualCtx);
            Assert.assertEquals(expectedCtx, actualCtx);
        }
    }

}
