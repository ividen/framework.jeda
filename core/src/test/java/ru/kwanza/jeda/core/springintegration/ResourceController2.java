package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.internal.IResourceController;

/**
 * @author Guzanov Alexander
 */
public class ResourceController2 implements IResourceController {
    private Long p1;
    private Long p2;

    public ResourceController2(Long p1, Long p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public void throughput(int count, int batchSize, long millis, boolean success) {

    }

    public void input(long count) {

    }

    public double getInputRate() {
        return 0;
    }

    public double getThroughputRate() {
        return 0;
    }

    public int getBatchSize() {
        return 0;
    }

    public int getThreadCount() {
        return 0;
    }

    public static ResourceController2 create(Long p1, Long p2) {
        return new ResourceController2(p1, p2);
    }
}
