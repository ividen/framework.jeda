package ru.kwanza.jeda.clusterservice.mock;

import ru.kwanza.jeda.clusterservice.old.INodeListener;
import ru.kwanza.jeda.clusterservice.old.impl.mock.MockClusterServiceImpl;
import junit.framework.TestCase;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Guzanov Alexander
 */
public class TestMockClusterServiceImpl extends TestCase {

    public void testSetGetLastActivity() {
        assertEquals(0, MockClusterServiceImpl.getInstance().getLastNodeActivity(1));
        long ts = System.currentTimeMillis();
        MockClusterServiceImpl.getInstance().setLastNodeActivity(1, ts);
        assertEquals(ts, MockClusterServiceImpl.getInstance().getLastNodeActivity(1));
    }

    public void testSetGetNodeId() {
        assertEquals(0, MockClusterServiceImpl.getInstance().getNodeId());
        MockClusterServiceImpl.getInstance().setNodeId(2);
        assertEquals(2, MockClusterServiceImpl.getInstance().getNodeId());
    }

    public void testClear() {
        long ts = System.currentTimeMillis();
        MockClusterServiceImpl.getInstance().setLastNodeActivity(1, ts);
        MockClusterServiceImpl.getInstance().setNodeId(2);
        assertEquals(2, MockClusterServiceImpl.getInstance().getNodeId());
        assertEquals(ts, MockClusterServiceImpl.getInstance().getLastNodeActivity(1));

        MockClusterServiceImpl.getInstance().clear();

        assertEquals(0, MockClusterServiceImpl.getInstance().getLastNodeActivity(1));
        assertEquals(0, MockClusterServiceImpl.getInstance().getNodeId());
    }


    public void testSubscribes() {

        MockClusterServiceImpl.getInstance().clear();

        final AtomicLong nodeLost = new AtomicLong(0l);
        final AtomicLong nodeActivate = new AtomicLong(0l);
        final AtomicLong currentNodeLost = new AtomicLong(0l);
        final AtomicLong currentNodeActivate = new AtomicLong(0l);

        INodeListener firstListener = new INodeListener() {
            public void onNodeLost(Long nodeId, long lastNodeTs) {
                nodeLost.incrementAndGet();
            }

            public void onNodeActivate(Long nodeId, long lastNodeTs) {
                nodeActivate.incrementAndGet();
            }

            public void onCurrentNodeActivate() {
                currentNodeActivate.incrementAndGet();
            }

            public void onCurrentNodeLost() {
                currentNodeLost.incrementAndGet();
            }
        };


        INodeListener secondListener = new INodeListener() {
            public void onNodeLost(Long nodeId, long lastNodeTs) {
                nodeLost.incrementAndGet();
            }

            public void onNodeActivate(Long nodeId, long lastNodeTs) {
                nodeActivate.incrementAndGet();
            }

            public void onCurrentNodeActivate() {
                currentNodeActivate.incrementAndGet();
            }

            public void onCurrentNodeLost() {
                currentNodeLost.incrementAndGet();
            }
        };

        MockClusterServiceImpl.getInstance().subscribe(firstListener);
        MockClusterServiceImpl.getInstance().subscribe(secondListener);

        MockClusterServiceImpl.getInstance().generateNodeLost(1, 1);
        MockClusterServiceImpl.getInstance().generateNodeActivate(1, 1);
        MockClusterServiceImpl.getInstance().generateNodeActivate(1, 1);
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        MockClusterServiceImpl.getInstance().generateCurrentNodeLost();
        MockClusterServiceImpl.getInstance().generateCurrentNodeLost();
        MockClusterServiceImpl.getInstance().generateCurrentNodeLost();
        MockClusterServiceImpl.getInstance().generateCurrentNodeLost();


        assertEquals("Wrong nodeLost count", 2, nodeLost.get());
        assertEquals("Wrong nodeActivate count", 4, nodeActivate.get());
        assertEquals("Wrong currentNodeActivate count", 6, currentNodeActivate.get());
        assertEquals("Wrong currentNodeLost count", 8, currentNodeLost.get());

        MockClusterServiceImpl.getInstance().unSubscribe(firstListener);

        MockClusterServiceImpl.getInstance().generateNodeLost(1, 1);
        MockClusterServiceImpl.getInstance().generateNodeActivate(1, 1);
        MockClusterServiceImpl.getInstance().generateNodeActivate(1, 1);
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        MockClusterServiceImpl.getInstance().generateCurrentNodeActivate();
        MockClusterServiceImpl.getInstance().generateCurrentNodeLost();
        MockClusterServiceImpl.getInstance().generateCurrentNodeLost();
        MockClusterServiceImpl.getInstance().generateCurrentNodeLost();
        MockClusterServiceImpl.getInstance().generateCurrentNodeLost();

        assertEquals("Wrong nodeLost count", 3, nodeLost.get());
        assertEquals("Wrong nodeActivate count", 6, nodeActivate.get());
        assertEquals("Wrong currentNodeActivate count", 9, currentNodeActivate.get());
        assertEquals("Wrong currentNodeLost count", 12, currentNodeLost.get());


    }
}
