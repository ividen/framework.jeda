package ru.kwanza.jeda.context.berkeley;

import ru.kwanza.jeda.context.MapContextImpl;
import junit.framework.Assert;
import junit.framework.TestCase;

public class TestBerkeleyBlobContextController extends TestCase {

    private BerkeleyBlobContextController controller;

    public void setUp() throws Exception {
        controller = new BerkeleyBlobContextController();
    }

    public void testCreateEmptyValueNoTerm() throws Exception {
        final String ctxId = "testId";
        MapContextImpl expectedCtx = new MapContextImpl(ctxId, null, null);
        MapContextImpl ctx = controller.createEmptyValue("testId");
        Assert.assertEquals(expectedCtx, ctx);
    }

    public void testCreateEmptyValueWithTerm() throws Exception {
        final String ctxId = "testId";
        final String term = "testTerm";
        MapContextImpl expectedCtx = new MapContextImpl(ctxId, term, null);

        controller.setTerminator(term);
        MapContextImpl ctx = controller.createEmptyValue("testId");

        Assert.assertEquals(expectedCtx, ctx);
    }

}
