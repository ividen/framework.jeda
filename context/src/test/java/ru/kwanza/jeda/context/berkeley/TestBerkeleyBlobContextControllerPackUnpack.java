package ru.kwanza.jeda.context.berkeley;

import ru.kwanza.jeda.context.MapContextImpl;
import ru.kwanza.toolbox.SerializationHelper;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry Zagorovsky
 */
public class TestBerkeleyBlobContextControllerPackUnpack extends TestCase {

    private static final String TERMINATOR_NAME = "termName1";
    protected static final Long VERSION = 1020l;

    private BerkeleyBlobContextController ctxController;

    @Override
    public void setUp() throws Exception {
        ctxController = new BerkeleyBlobContextController();
        ctxController.setTerminator(TERMINATOR_NAME);

    }

    public void testCtxDataPack() throws Exception {
        final MapContextImpl ctx = getTestContext();

        final byte[] packedData = ctxController.packContext(ctx);

        ByteBuffer bb = ByteBuffer.wrap(packedData);
        Assert.assertEquals(VERSION, new Long(bb.getLong()));

        byte[] dataBytes = new byte[bb.remaining()];
        bb.get(dataBytes);

        @SuppressWarnings("unchecked")
        Map<String, String> actualData = (Map<String, String>) SerializationHelper.bytesToObject(dataBytes);

        Assert.assertEquals(ctx.getInnerMap(), actualData);
    }

    public void testCtxUnpack() throws Exception {
        MapContextImpl ctx = getTestContext();
        byte[] data = ctxController.packContext(ctx);
        Assert.assertEquals(getTestContext(), ctxController.unpackContext(ctx.getId(), data));
    }

    public void testVersionUnpack() throws Exception {
        MapContextImpl ctx = getTestContext();
        byte[] data = ctxController.packContext(ctx);
        Assert.assertEquals(VERSION, ctxController.unpackContextVersion(data));
    }

    private MapContextImpl getTestContext() {
        Map<String, Object> keyByValue = new HashMap<String, Object>();
        keyByValue.put("key1", "value1");
        keyByValue.put("key2", "value2");
        keyByValue.put("key3", "value3");

        return new MapContextImpl("ctxId1", TERMINATOR_NAME, VERSION, keyByValue);
    }

}