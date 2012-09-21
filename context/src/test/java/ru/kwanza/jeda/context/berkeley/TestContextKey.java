package ru.kwanza.jeda.context.berkeley;

import ru.kwanza.jeda.context.MapContextImpl;
import junit.framework.Assert;
import junit.framework.TestCase;

public class TestContextKey extends TestCase {

    private static final String ctxId = "testCtxId";
    private static final String term = "term";
    private ContextKey contextKey;

    public void testConstructor1() throws Exception {
        contextKey = new ContextKey(ctxId, term);
        Assert.assertEquals(ctxId, contextKey.getContextId());
        Assert.assertEquals(term, contextKey.getTerminator());
    }

    public void testConstructor2() throws Exception {
        MapContextImpl ctx = new MapContextImpl(ctxId, term, null);
        contextKey = new ContextKey(ctx);
        Assert.assertEquals(ctxId, contextKey.getContextId());
        Assert.assertEquals(term, contextKey.getTerminator());
    }

}

