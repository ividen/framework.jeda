package ru.kwanza.jeda.context;

import ru.kwanza.dbtool.VersionGenerator;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.*;

public class TestMapContextImpl extends TestCase {

    private final String id = "id";
    private final String terminator = "terminator";
    private final Long version = 123l;

    private static final String KEY_1 = "key1";
    private static final String KEY_2 = "key2";
    private static final String KEY_3 = "key3";
    private static final String VALUE_1 = "value1";
    private static final String VALUE_2 = "value2";
    private static final String VALUE_3 = "value3";

    public void testConstruct1() throws Exception {
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        MapContextImpl mapContext = new MapContextImpl(id, terminator, version);
        Assert.assertEquals(id, mapContext.getId());
        Assert.assertEquals(terminator, mapContext.getTerminator());
        Assert.assertEquals(version, mapContext.getVersion());
    }

    public void testConstruct2() throws Exception {
        MapContextImpl mapContext = new MapContextImpl(id, terminator, version, getTestMap());
        Assert.assertEquals(id, mapContext.getId());
        Assert.assertEquals(terminator, mapContext.getTerminator());
        Assert.assertEquals(version, mapContext.getVersion());
        Assert.assertEquals(getTestMap(), mapContext.getInnerMap());
    }

    public void testSetId() throws Exception {
        MapContextImpl mapContext = new MapContextImpl(id, terminator, version);

        final String newId = "newId";

        mapContext.setId(newId);
        Assert.assertEquals(newId, mapContext.getId());
    }

    public void testSetTerminator() throws Exception {
        MapContextImpl mapContext = new MapContextImpl(id, terminator, version);

        final String newTerm = "newTerm";

        mapContext.setTerminator(newTerm);
        Assert.assertEquals(newTerm, mapContext.getTerminator());
    }

    public void testSetVersion() throws Exception {
        MapContextImpl mapContext = new MapContextImpl(id, terminator, version);

        final Long newVersion = 222l;

        mapContext.setVersion(newVersion);
        Assert.assertEquals(newVersion, mapContext.getVersion());
    }

    public void testGetSize() throws Exception {
        MapContextImpl mapContext = new MapContextImpl(id, terminator, version, getTestMap());
        Assert.assertEquals(getTestMap().size(), mapContext.size());
    }

    public void testIsEmpty() throws Exception {
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        MapContextImpl mapContext = new MapContextImpl(id, terminator, version);
        Assert.assertTrue(mapContext.isEmpty());
        Assert.assertEquals(0, mapContext.size());
    }

    public void testContainsKey() throws Exception {
        MapContextImpl mapContext = new MapContextImpl(id, terminator, version, getTestMap());
        Assert.assertTrue(mapContext.containsKey(KEY_1));
    }

    public void testContainsValue() throws Exception {
        MapContextImpl mapContext = new MapContextImpl(id, terminator, version, getTestMap());
        Assert.assertTrue(mapContext.containsValue(VALUE_1));
    }

    public void testGet() throws Exception {
        MapContextImpl mapContext = new MapContextImpl(id, terminator, version, getTestMap());
        assertCtxContainsTestData(mapContext);
    }

    public void testPut() throws Exception {
        MapContextImpl mapContext = new MapContextImpl(id, terminator, version);
        Assert.assertNull(mapContext.put(KEY_1, VALUE_1));
        Assert.assertNull(mapContext.put(KEY_2, VALUE_2));
        Assert.assertNull(mapContext.put(KEY_3, VALUE_3));

        assertCtxContainsTestData(mapContext);
    }

    public void testRemove() throws Exception {
        MapContextImpl mapContext = new MapContextImpl(id, terminator, version, getTestMap());

        Assert.assertEquals(VALUE_1, mapContext.remove(KEY_1));
        Assert.assertEquals(VALUE_2, mapContext.remove(KEY_2));
        Assert.assertEquals(VALUE_3, mapContext.remove(KEY_3));

        Assert.assertNull(mapContext.remove("UNKNOWN_KEY"));
    }

    public void testPutAll() throws Exception {
        MapContextImpl mapContext = new MapContextImpl(id, terminator, version);

        Assert.assertTrue(mapContext.isEmpty());

        mapContext.putAll(getTestMap());
        assertCtxContainsTestData(mapContext);
    }

    public void testClear() throws Exception {
        MapContextImpl mapContext = new MapContextImpl(id, terminator, version, getTestMap());
        mapContext.clear();
        Assert.assertTrue(mapContext.isEmpty());
    }

    public void testKeySet() throws Exception {
        MapContextImpl mapContext = new MapContextImpl(id, terminator, version, getTestMap());

        Set<String> expectedKeySet = new HashSet<String>();
        expectedKeySet.add(KEY_1);
        expectedKeySet.add(KEY_2);
        expectedKeySet.add(KEY_3);

        Assert.assertEquals(3, mapContext.keySet().size());
        Assert.assertEquals(expectedKeySet, mapContext.keySet());
    }

    public void testValues() throws Exception {
        MapContextImpl mapContext = new MapContextImpl(id, terminator, version, getTestMap());

        Set<String> expectedValues = new HashSet<String>();
        expectedValues.add(VALUE_1);
        expectedValues.add(VALUE_2);
        expectedValues.add(VALUE_3);

        Collection actualValues = mapContext.values();
        Set<String> actualValueSet = new HashSet<String>();

        for (Object obj : actualValues) {
            actualValueSet.add((String) obj);
        }

        Assert.assertEquals(expectedValues, actualValueSet);
    }

    public void testGetInnerMap() throws Exception {
        MapContextImpl mapContext = new MapContextImpl(id, terminator, version, getTestMap());
        Assert.assertEquals(getTestMap(), mapContext.getInnerMap());
    }

    public void testEntrySet() throws Exception {
        MapContextImpl mapContext = new MapContextImpl(id, terminator, version, getTestMap());
        Assert.assertEquals(getTestMap().entrySet(), mapContext.entrySet());
    }

    public void testEquals() throws Exception {
        MapContextImpl mapContext1 = new MapContextImpl(id, terminator, version, getTestMap());
        MapContextImpl mapContext2 = new MapContextImpl(id, terminator, version, getTestMap());
        Assert.assertTrue(mapContext1.equals(mapContext2));
    }

    public void testKeyField() throws Exception {
        MapContextImpl mapContext = new MapContextImpl(id, terminator, version);
        Assert.assertEquals(mapContext.getId(), MapContextImpl.KEY.value(mapContext));
    }

    public void testVersionField() throws Exception {
        MapContextImpl mapContext = new MapContextImpl(id, terminator, version);
        Assert.assertEquals(mapContext.getId(), MapContextImpl.KEY.value(mapContext));

        VersionGenerator versionGenerator = new VersionGenerator();
        MapContextImpl.VersionFieldImpl versionField = new MapContextImpl.VersionFieldImpl(versionGenerator);
        Assert.assertEquals(version, versionField.value(mapContext));
        Assert.assertNotNull(versionField.generateNewValue(mapContext));

        Long newVersion = 222l;
        versionField.setValue(mapContext, newVersion);
        Assert.assertEquals(newVersion, versionField.value(mapContext));
    }

    private void assertCtxContainsTestData(MapContextImpl mapContext) {
        Assert.assertEquals(3, mapContext.size());
        Assert.assertEquals(VALUE_1, mapContext.get(KEY_1));
        Assert.assertEquals(VALUE_2, mapContext.get(KEY_2));
        Assert.assertEquals(VALUE_3, mapContext.get(KEY_3));
    }

    private Map<String, Object> getTestMap() {
        Map<String, Object> objByKey = new HashMap<String, Object>();
        objByKey.put("key1", "value1");
        objByKey.put("key2", "value2");
        objByKey.put("key3", "value3");

        return objByKey;
    }

}
