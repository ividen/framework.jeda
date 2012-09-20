package ru.kwanza.jeda.context.berkeley;

import ru.kwanza.jeda.api.ContextStoreException;
import ru.kwanza.jeda.context.AbstractBlobContextControllerTest;
import ru.kwanza.jeda.context.MapContextImpl;
import junit.framework.Assert;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitry Zagorovsky
 */
public abstract class AbstractBerkeleyBlobContextControllerTest extends AbstractBlobContextControllerTest {


    protected abstract String getContextFileName();

    @Override
    protected void makeBeforeCtxInit() throws Exception {
        clean();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        clean();
    }

    public void testContextUpdateCantAcquire() throws Exception {
        List<MapContextImpl> testContextList = Arrays.asList(
                getTestContextList(null).get(0),
                getTestContextList(null).get(2),
                getTestContextList(null).get(4));

        ctxController.store(testContextList);

        List<MapContextImpl> updatedCtxList = getTestContextList(1l);
        for (MapContextImpl context : updatedCtxList) {
            context.putAll(getTestMap("updated", 3));
        }

        try {
            ctxController.store(updatedCtxList);
            fail("No ContextStoreException, but must be!");
        } catch (ContextStoreException e) {
            Assert.assertEquals(2, e.getOtherFailedItems().size());
        }

        List<MapContextImpl> expectedCtxList = Arrays.asList(
                getTestContextList(1l).get(0),
                getTestContextList(1l).get(2),
                getTestContextList(1l).get(4));
        expectedCtxList.get(0).putAll(getTestMap("updated", 3));
        expectedCtxList.get(1).putAll(getTestMap("updated", 3));
        expectedCtxList.get(2).putAll(getTestMap("updated", 3));

        Map<String, MapContextImpl> actualMap = ctxController.load(Arrays.asList(CONTEXT_IDS));
        assertContextMapEqualsWithoutVersion(getContextListAsMap(expectedCtxList), actualMap);
    }




    protected void clean() throws Exception {
        delete(new File("./target/test_berkeley_db"));
    }

    private void delete(File file) throws IOException {
        if (file.isDirectory()) {
            for (File item : file.listFiles()) {
                delete(item);
            }
        }
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

}
