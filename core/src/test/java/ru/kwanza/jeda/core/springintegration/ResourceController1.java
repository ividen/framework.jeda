package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.internal.IResourceController;

/**
 * @author Guzanov Alexander
 */
public class ResourceController1 implements IResourceController {
    private String s1;
    private String s2;

    public ResourceController1(String s1, String s2) {
        this.s1 = s1;
        this.s2 = s2;
    }

    public void throughput(int count, int batchSize, long millis, boolean success) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void input(long count) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public double getInputRate() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public double getThroughputRate() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getBatchSize() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getThreadCount() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
