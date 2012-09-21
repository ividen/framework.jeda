package ru.kwanza.jeda.context.jdbc;

import ru.kwanza.dbtool.UpdateException;
import ru.kwanza.jeda.api.ContextStoreException;
import ru.kwanza.jeda.context.MapContextImpl;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TestAbstractJDBCContextController extends TestCase {

    private TestJDBCContextController ctxController = new TestJDBCContextController();

    public void testInitIfAbsent() throws Exception {
        List<MapContextImpl> list = null;
        List<MapContextImpl> list2 = ctxController.initIfAbsent(list);
        Assert.assertNotNull(list2);
        Assert.assertEquals(list2, ctxController.initIfAbsent(list2));
    }

    public void testSetIdColumnName() throws Exception {
        final String idColumnName = "idColName";
        ctxController.setIdColumnName(idColumnName);
        Assert.assertEquals(idColumnName, ctxController.getIdColumnName());
    }

    public void testSetTableName() throws Exception {
        final String tableName = "tableName";
        ctxController.setTableName(tableName);
        Assert.assertEquals(tableName, ctxController.getTableName());
    }

    public void testSetTerminator() throws Exception {
        final String terminator = "term";
        ctxController.setTerminator(terminator);
        Assert.assertEquals(terminator, ctxController.getTerminator());
    }

    public void testSetVersionColumnName() throws Exception {
        final String versionColumnName = "verColName";
        ctxController.setVersionColumnName(versionColumnName);
        Assert.assertEquals(versionColumnName, ctxController.getVersionColumnName());
    }

    public void testSetTerminatorColumnName() throws Exception {
        final String terminatorColumnName = "verColName";
        ctxController.setTerminatorColumnName(terminatorColumnName);
        Assert.assertEquals(terminatorColumnName, ctxController.getTerminatorColumnName());
    }


    private static class TestJDBCContextController extends AbstractJDBCContextController<String, MapContextImpl> {


        @Override
        protected void setContextVersion(MapContextImpl context, Long version) {
        }

        @Override
        protected void storeNewContextItems(List<MapContextImpl> contextObjects) throws ContextStoreException {
        }

        @Override
        protected void updateContextItems(List<MapContextImpl> contextObjects) throws ContextStoreException {
        }

        @Override
        protected void blockBeforeRemove(Collection<MapContextImpl> contexts) throws UpdateException {
        }

        @Override
        protected void removeBlocked(Collection<MapContextImpl> contextToRemoveList) throws UpdateException {
        }

        @Override
        protected void initSqlBuilder() {
        }

        public MapContextImpl createEmptyValue(String contextId) {
            return null;
        }

        public Map<String, MapContextImpl> load(Collection<String> contextIds) {
            return null;
        }
    }

}
