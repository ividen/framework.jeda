package ru.kwanza.jeda.core.resourcecontroller;

import ru.kwanza.jeda.api.internal.IResourceController;

/**
 * @author Guzanov Alexander
 */
public class StaticResourceController implements IResourceController {
    private InputRateHelper inputRateHelper;
    private ThroughputRateHelper throughputRateHelper;
    private volatile int batchSize = 1000;
    private volatile int threadCount = Runtime.getRuntime().availableProcessors();
    private RateAdjustInfo adjustInfo;

    public StaticResourceController() {
        this.adjustInfo = new RateAdjustInfo(1000, 1000);
        this.inputRateHelper = new InputRateHelper(this.adjustInfo);
        this.throughputRateHelper = new ThroughputRateHelper(this.adjustInfo);
    }

    public final void throughput(int count, int batchSize, long millis, boolean success) {
        if (!success) {
            return;
        }
        throughputRateHelper.calculate(count, batchSize, millis);
    }

    public final void input(long count) {
        inputRateHelper.calculate(count);
    }

    public final double getInputRate() {
        return inputRateHelper.getRate();
    }

    public final double getThroughputRate() {
        return throughputRateHelper.getRealRate();
    }

    public int getThreadCount() {
        return threadCount;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setAdjustmentCount(int adjustCount) {
        this.adjustInfo.setCount(adjustCount);
    }

    public void setAdjustmentInterval(int adjustInterval) {
        this.adjustInfo.setInterval(adjustInterval);
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }
}
