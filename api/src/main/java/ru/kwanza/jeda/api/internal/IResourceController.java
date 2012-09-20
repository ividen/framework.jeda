package ru.kwanza.jeda.api.internal;

/**
 * @author Guzanov Alexander
 */
public interface IResourceController {

    public void throughput(int count, int batchSize, long millis, boolean success);

    public void input(long count);

    public double getInputRate();

    public double getThroughputRate();

    public int getBatchSize();

    public int getThreadCount();
}
