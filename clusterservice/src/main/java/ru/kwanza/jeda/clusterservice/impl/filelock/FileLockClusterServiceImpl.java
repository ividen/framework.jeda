package ru.kwanza.jeda.clusterservice.impl.filelock;

import ru.kwanza.jeda.clusterservice.INodeListener;
import ru.kwanza.jeda.clusterservice.impl.IClusterServiceImpl;

/**
 * @author Guzanov Alexander
 */
public class FileLockClusterServiceImpl implements IClusterServiceImpl {
    public long getLastNodeActivity(long nodeId) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public long getNodeId() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void subscribe(INodeListener listener) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void unSubscribe(INodeListener listener) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
