package ru.kwanza.jeda.core.pendingstore;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.List;

/**
 * @author Dmitry Zagorovsky
 */
public class TestUnitDefaultPendingStore extends TestCase {

    public void testInitIfAbsent() throws Exception {
        DefaultPendingStore pendingStore = new DefaultPendingStore();

        List<Long> list = null;
        List<Long> list2 = pendingStore.initIfAbsent(list);
        Assert.assertNotNull(list2);
        Assert.assertEquals(list2, pendingStore.initIfAbsent(list2));
    }

}
