package ru.kwanza.jeda.clusterservice.impl.mock;

import ru.kwanza.jeda.clusterservice.INodeListener;
import ru.kwanza.jeda.clusterservice.impl.IClusterServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Guzanov Alexander
 */
public class MockClusterServiceImpl implements IClusterServiceImpl {
    private static final MockClusterServiceImpl instance = new MockClusterServiceImpl();

    private long nodeId;
    private ArrayList<INodeListener> listeners = new ArrayList<INodeListener>();
    private Map<Long, Long> activity = new HashMap<Long, Long>();

    private MockClusterServiceImpl() {
    }

    public static MockClusterServiceImpl getInstance() {
        return instance;
    }

    public long getLastNodeActivity(long nodeId) {
        Long result = activity.get(nodeId);
        return result == null ? 0 : result;
    }

    public void setLastNodeActivity(long nodeId, long ts) {
        activity.put(nodeId, ts);
    }

    public long getNodeId() {
        return nodeId;
    }

    public void setNodeId(long nodeId) {
        this.nodeId = nodeId;
    }

    public void subscribe(INodeListener listener) {
        listeners.add(listener);
    }

    public void unSubscribe(INodeListener listener) {
        listeners.remove(listener);
    }

    public void clear() {
        nodeId = 0;
        activity.clear();
        listeners.clear();
    }


    public void generateCurrentNodeActivate() {
        for (INodeListener nl : listeners) {
            nl.onCurrentNodeActivate();
        }
    }

    public void generateNodeLost(long nodeId, long ts) {
        for (INodeListener nl : listeners) {
            nl.onNodeLost(nodeId, ts);
        }
    }

    public void generateNodeActivate(long nodeId, long ts) {
        for (INodeListener nl : listeners) {
            nl.onNodeActivate(nodeId, ts);
        }
    }

    public void generateCurrentNodeLost() {
        for (INodeListener nl : listeners) {
            nl.onCurrentNodeLost();
        }
    }


}
