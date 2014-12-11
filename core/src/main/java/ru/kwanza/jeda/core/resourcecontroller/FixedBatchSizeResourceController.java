package ru.kwanza.jeda.core.resourcecontroller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kwanza.jeda.api.internal.AbstractResourceController;

/**
 * @author Guzanov Alexander
 */
public class FixedBatchSizeResourceController extends AbstractResourceController {

    private static final Logger logger = LoggerFactory.getLogger(FixedBatchSizeResourceController.class);
    private InputRateHelper inputRateHelper;
    private ThroughputRateHelper throughputRateHelper;
    private volatile int batchSize;
    private volatile int threadCount;
    private int maxThreadCount;
    private RateAdjustInfo adjustInfo;
    private long waitForFillingTimeout;

    public FixedBatchSizeResourceController(int batchSize, int maxThreadCount) {
        this.batchSize = batchSize;
        this.adjustInfo = new RateAdjustInfo(1000, 1000);
        this.inputRateHelper = new InputRateHelper(this.adjustInfo);
        this.throughputRateHelper = new ThroughputRateHelper(this.adjustInfo);
        this.waitForFillingTimeout = -1;
        this.maxThreadCount = maxThreadCount;
    }

    public FixedBatchSizeResourceController(int batchSize) {
        this(batchSize, Runtime.getRuntime().availableProcessors());
    }

    public final void throughput(int count, int batchSize, long millis, boolean success) {
        if (success) {
            throughputRateHelper.calculate(count, batchSize, millis);
        }

        adjustThreadCount();
        traceState(batchSize);
    }

    public final void input(long count) {
        inputRateHelper.calculate(count);
        adjustThreadCount();
        traceState(batchSize);
    }

    private void adjustThreadCount() {
        if (getStage().getQueue().getEstimatedCount() > batchSize) {
            int threadCount = (getStage().getQueue().getEstimatedCount() / batchSize);
            if (threadCount > maxThreadCount) {
                threadCount = maxThreadCount;
            }

            this.threadCount = threadCount;

        } else {
            if (waitForFillingTimeout <= 0) {
                this.threadCount = 1;
            } else {
                WaitingForBatchFilling.getInstance().schedule(getStage(), getMaxThreadCount(), waitForFillingTimeout);
                this.threadCount = 0;
            }
        }
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

    public void setAdjustmentCount(int adjustmentCount) {
        this.adjustInfo.setCount(adjustmentCount);
    }

    public void setAdjustmentInterval(int adjustInterval) {
        this.adjustInfo.setInterval(adjustInterval);
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getMaxThreadCount() {
        return maxThreadCount;
    }

    public void setMaxThreadCount(int maxThreadCount) {
        this.maxThreadCount = maxThreadCount;
    }

    public long getWaitForFillingTimeout() {
        return waitForFillingTimeout;
    }

    public void setWaitForFillingTimeout(long waitForFillingTimeout) {
        this.waitForFillingTimeout = waitForFillingTimeout;
    }

    private void traceState(int batchSize) {
        if (logger.isTraceEnabled()) {
            logger.trace("Stage {} , batchSize= {}, input={}, thoughput={},"
                            + " realThroughput={}, queueSize={} , threadCount={}",
                    new Object[]{getStage().getName(), batchSize, inputRateHelper.getRate(),
                            throughputRateHelper.getRate(), throughputRateHelper.getRealRate(),
                            getStage().getQueue().getEstimatedCount(), this.threadCount});
        }
    }
}
