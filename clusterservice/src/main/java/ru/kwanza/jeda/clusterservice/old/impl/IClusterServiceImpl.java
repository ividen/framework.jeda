package ru.kwanza.jeda.clusterservice.old.impl;

import ru.kwanza.jeda.clusterservice.old.INodeListener;

/**
 * @author Guzanov Alexander
 */
public interface IClusterServiceImpl {
    public long getLastNodeActivity(long nodeId);

    public long getNodeId();

    public void subscribe(INodeListener listener);

    public void unSubscribe(INodeListener listener);
}