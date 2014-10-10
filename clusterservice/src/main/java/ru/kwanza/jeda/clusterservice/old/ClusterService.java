package ru.kwanza.jeda.clusterservice.old;

import ru.kwanza.jeda.clusterservice.old.impl.IClusterServiceImpl;

import javax.annotation.Resource;

/**
 * @author Guzanov Alexander
 */
public class ClusterService {
    private static final ClusterService instance = new ClusterService();

    @Resource(name = "clusterServiceImpl")
    private IClusterServiceImpl impl;

    public static long getNodeId() {
        return instance.impl.getNodeId();
    }

    public static long getLastNodeActivity(long nodeId) {
        return instance.impl.getLastNodeActivity(nodeId);
    }

    public static void subscribe(INodeListener listener) {
        instance.impl.subscribe(listener);
    }

    public static void unSubscribe(INodeListener listener) {
        instance.impl.unSubscribe(listener);
    }

    public static ClusterService getInstance() {
        return instance;
    }
}
