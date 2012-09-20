package ru.kwanza.jeda.context;

import ru.kwanza.jeda.api.ContextStoreException;
import ru.kwanza.jeda.api.IContextController;
import ru.kwanza.jeda.context.berkeley.BerkeleyBlobContextController;
import ru.kwanza.jeda.context.jdbc.JDBCBlobContextController;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.*;

/**
 * @author Dmitry Zagorovsky
 */
public abstract class AbstractBlobContextControllerTest extends TestCase {

    protected static final String[] CONTEXT_IDS = new String[]{"context1", "context2", "context3", "context4", "context5"};

    protected ApplicationContext ctx;
    protected IContextController<String, MapContextImpl> ctxController;

    protected abstract String getContextFileName();

    @Override
    public void setUp() throws Exception {
        makeBeforeCtxInit();
        ctx = new ClassPathXmlApplicationContext(getContextFileName(), this.getClass());
    }

    @Override
    public void tearDown() throws Exception {
        ((ClassPathXmlApplicationContext) ctx).close();
    }

    public void testSimpleContextStore() throws Exception {
        List<MapContextImpl> lst = getTestContextList(null);
        ctxController.store(lst);

        //�������� ���������� ������ ��� ������ ����������
        Assert.assertEquals(getTestContextList(1l), lst);

        Map<String, MapContextImpl> actualMap = ctxController.load(Arrays.asList(CONTEXT_IDS));
        Assert.assertEquals(getContextListAsMap(getTestContextList(1l)), actualMap);
    }

    public void testSimpleContextDelete() throws Exception {
        List<MapContextImpl> testContextList = getTestContextList(null);
        ctxController.store(testContextList);
        Assert.assertEquals(CONTEXT_IDS.length, ctxController.load(Arrays.asList(CONTEXT_IDS)).size());

        // �������� ��� ��������.
        List<MapContextImpl> listToRemove = Arrays.asList(
                getTestContextList(1l).get(0),
                getTestContextList(1l).get(1));

        ctxController.remove(listToRemove);

        // ��������, ������� ��������� � ��.
        List<MapContextImpl> expectedList = Arrays.asList(
                getTestContextList(1l).get(2),
                getTestContextList(1l).get(3),
                getTestContextList(1l).get(4));

        Map<String, MapContextImpl> actualMap = ctxController.load(Arrays.asList(CONTEXT_IDS));
        Assert.assertEquals(getContextListAsMap(expectedList), actualMap);
    }


    public void testContextDeleteWithOptimistic() throws Exception {
        List<MapContextImpl> testContextList = getTestContextList(null);
        ctxController.store(testContextList);
        Assert.assertEquals(CONTEXT_IDS.length, ctxController.load(Arrays.asList(CONTEXT_IDS)).size());

        // �������� ��� ��������. � ���� ������ ������ ���������� �� ��.
        List<MapContextImpl> listToRemove = Arrays.asList(
                getTestContextList(2l).get(0),
                getTestContextList(2l).get(1),
                getTestContextList(1l).get(2));

        // �� ���� ��������� ����� ����������.
        List<MapContextImpl> optimisticLockList = Arrays.asList(
                getTestContextList(2l).get(0),
                getTestContextList(2l).get(1));

        try {
            ctxController.remove(listToRemove);
            fail("No optimistic lock, but must be!");
        } catch (ContextStoreException e) {
            Assert.assertEquals(optimisticLockList, e.getOptimisticItems());
            Assert.assertNull(e.getOtherFailedItems());
        }

        // ��������, ������� ������ �������� � �� ����� ��������.
        List<MapContextImpl> expectedList = Arrays.asList(
                getTestContextList(1l).get(0),
                getTestContextList(1l).get(1),
                getTestContextList(1l).get(3),
                getTestContextList(1l).get(4));


        Map<String, MapContextImpl> actualMap = ctxController.load(Arrays.asList(CONTEXT_IDS));
        Assert.assertEquals(getContextListAsMap(expectedList), actualMap);
    }

    public void testSimpleContextUpdate() throws Exception {
        List<MapContextImpl> testContextList = getTestContextList(null);
        ctxController.store(testContextList);

        for (MapContextImpl context : testContextList) {
            context.putAll(getTestMap("updated", 3));
            context.setVersion(1l);
        }

        ctxController.store(testContextList);

        Map<String, MapContextImpl> actualMap = ctxController.load(Arrays.asList(CONTEXT_IDS));

        Assert.assertEquals(getContextListAsMap(testContextList), actualMap);
    }

    public void testContextUpdateWithOptimistic() throws Exception {
        List<MapContextImpl> testContextList = getTestContextList(null);
        ctxController.store(testContextList);

        // ��������� ��� ����������
        List<MapContextImpl> updatedCtxList = getTestContextList(1l);
        for (MapContextImpl context : updatedCtxList) {
            context.putAll(getTestMap("updated", 3));
        }
        // ������ 1, 3 � 5�� ��������� ����� ��������� �� ������ � ��
        updatedCtxList.get(0).setVersion(5l);
        updatedCtxList.get(2).setVersion(5l);
        updatedCtxList.get(4).setVersion(5l);

        // ������ optimistic'��.
        List<MapContextImpl> lockedItems = Arrays.asList(
                getTestContextList(5l).get(0),
                getTestContextList(5l).get(2),
                getTestContextList(5l).get(4));
        for (MapContextImpl context : lockedItems) {
            context.putAll(getTestMap("updated", 3));
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

        List<MapContextImpl> expectedCtxList = getTestContextList(1l);
        expectedCtxList.get(1).putAll(getTestMap("updated", 3));
        expectedCtxList.get(3).putAll(getTestMap("updated", 3));

        Map<String, MapContextImpl> actualMap = ctxController.load(Arrays.asList(CONTEXT_IDS));
        assertContextMapEqualsWithoutVersion(getContextListAsMap(expectedCtxList), actualMap);
    }

    public void testContextInsertConstrained() throws Exception {
        List<MapContextImpl> testContextList = getTestContextList(null);

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

    protected Map<String, MapContextImpl> getContextListAsMap(List<MapContextImpl> contextList) {
        Map<String, MapContextImpl> contextById = new HashMap<String, MapContextImpl>();
        for (MapContextImpl context : contextList) {
            contextById.put(context.getId(), context);
        }
        return contextById;
    }

    protected List<MapContextImpl> getTestContextList(Long version) {
        List<MapContextImpl> contextList = new ArrayList<MapContextImpl>();

        for (String id : CONTEXT_IDS) {

            String terminator = null;
            if (ctxController instanceof JDBCBlobContextController) {
                terminator = ((JDBCBlobContextController) ctxController).getTerminator();
            }

            if (ctxController instanceof BerkeleyBlobContextController) {
                terminator = ((BerkeleyBlobContextController) ctxController).getTerminator();
            }

            contextList.add(new MapContextImpl(id, terminator, version, getTestMap(id, 4)));
        }
        return contextList;
    }

    protected Map<String, Object> getTestMap(String keyValuePrefix, int size) {
        Map<String, Object> keyByValue = new HashMap<String, Object>();
        for (int i = 0; i < size; i++) {
            TestObject to = new TestObject(keyValuePrefix + i);
            keyByValue.put(keyValuePrefix + i, to);
        }
        return keyByValue;
    }

    protected void assertContextMapEqualsWithoutVersion(Map<String, MapContextImpl> expectedMap, Map<String, MapContextImpl> actualMap) {
        Assert.assertEquals(expectedMap.size(), actualMap.size());
        for (String key : expectedMap.keySet()) {
            MapContextImpl expectedCtx = expectedMap.get(key);
            MapContextImpl actualCtx = actualMap.get(key);
            Assert.assertNotNull(expectedCtx);
            Assert.assertNotNull(actualCtx);
            Assert.assertEquals(expectedCtx.getInnerMap(), actualCtx.getInnerMap());
        }
    }

    protected void makeBeforeCtxInit() throws Exception {

    }

}
