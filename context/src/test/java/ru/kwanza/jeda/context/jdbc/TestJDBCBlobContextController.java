package ru.kwanza.jeda.context.jdbc;

import ru.kwanza.jeda.context.MapContextImpl;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author Dmitry Zagorovsky
 */
public class TestJDBCBlobContextController extends TestCase {

    private JDBCBlobContextController ctxController;

    @Override
    public void setUp() throws Exception {
        ctxController = new JDBCBlobContextController();
    }

    public void testCreateEmptyValue() throws Exception {
        final String ctxId = "contextId";

        MapContextImpl ctx = ctxController.createEmptyValue(ctxId);

        MapContextImpl expectedCtx = new MapContextImpl(ctxId, null, null);
        Assert.assertEquals(expectedCtx, ctx);
    }

    public void testCreateEmptyValueWithTerm() throws Exception {
        final String ctxId = "contextId";
        final String terminator = "term";

        ctxController.setTerminator(terminator);
        MapContextImpl ctx = ctxController.createEmptyValue(ctxId);

        MapContextImpl expectedCtx = new MapContextImpl(ctxId, terminator, null);
        Assert.assertEquals(expectedCtx, ctx);
    }

}
