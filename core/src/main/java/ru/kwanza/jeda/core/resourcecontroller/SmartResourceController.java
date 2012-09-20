package ru.kwanza.jeda.core.resourcecontroller;

import ru.kwanza.jeda.api.internal.AbstractResourceController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import static ru.kwanza.jeda.core.resourcecontroller.Util.INCREASE_BATCH_MULTIPLIER;

/**
 * @author Guzanov Alexander
 */
public class SmartResourceController extends AbstractResourceController {
    public static final Logger logger = LoggerFactory.getLogger(SmartResourceController.class);

    private ArrayList<ThroughputInfo> throughputInfoList
            = new ArrayList<ThroughputInfo>();
    private HashMap<Integer, ThroughputInfo> findThroughputInfo
            = new HashMap<Integer, ThroughputInfo>();
    private InputRateHelper inputRateHelper;
    private RateAdjustInfo adjustInfo;

    private ReentrantLock lock = new ReentrantLock();

    private volatile int batchSize;
    private int maxBatchSize;
    private int maxElementCount;
    private int maxThreadCount;
    private long processingTimeThreshold;
    private long waitForFillingTimeout;
    private int threadCount;

    public SmartResourceController() {
        this(10);
    }

    public SmartResourceController(int startBatchSize) {
        this.maxThreadCount = Runtime.getRuntime().availableProcessors();
        this.adjustInfo = new RateAdjustInfo(1000, 1000, 4);
        this.inputRateHelper = new InputRateHelper(adjustInfo);
        this.maxElementCount = 1000;
        this.batchSize = 100;
        this.maxBatchSize = 10000;
        this.processingTimeThreshold = 60 * 1000;
        this.batchSize = startBatchSize;
        ThroughputInfo root = new ThroughputInfo(batchSize, adjustInfo, 1, 1, null, null);
        findThroughputInfo.put(batchSize, root);
        throughputInfoList.add(root);
        this.threadCount = 1;
    }

    public void throughput(int count, int batchSize, long millis, boolean success) {
        lock.lock();
        try {
            ThroughputInfo throughputInfo = findThroughputInfo.get(batchSize);
            if (throughputInfo != null && throughputInfo.calculate(count, batchSize, millis)) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Throughput for Stage({}) : batchSize={}, inputRate={}, throughputRate={}, realRate={}",
                            new Object[]{getStage().getName(), batchSize, inputRateHelper.getRate(),
                                    throughputInfo.getRate(), throughputInfo.getRealRate()});
                }
            }

            adjust();
        } finally {
            lock.unlock();
        }


    }

    private void adjust() {
        ThroughputInfo throughputInfo;
        throughputInfo = findThroughputInfo.get(this.batchSize);
        int oldBatch = throughputInfo.getBatchSize();
        if (needToAdjust(throughputInfo) && throughputInfo.isReady()) {
            makeAdjustment(throughputInfo);
            if (oldBatch == this.batchSize) {
                this.threadCount += 1;
                if (threadCount > getMaxThreadCount()) {
                    this.threadCount = getMaxThreadCount();
                }
            }
        } else if ((throughputInfo.getRate() - throughputInfo.getRealRate()) > 1) {
            this.threadCount -= 1;
            if (threadCount < 1) {
                this.threadCount = 1;
            }
        }
        System.out.println("BatchSize= " + batchSize +
                ", input=" + inputRateHelper.getRate() + ", thoughput =" + findThroughputInfo.get(batchSize).getRate()
                + ", realThoughput =" + findThroughputInfo.get(batchSize).getRealRate()
                + ", queueSize=" + getStage().getQueue().getEstimatedCount() + "  ThreadCount="
                + this.threadCount);
    }


    public void input(long count) {
        lock.lock();
        try {
            if (inputRateHelper.calculate(count)) {
                ThroughputInfo throughputInfo = findThroughputInfo.get(batchSize);
                if (logger.isTraceEnabled()) {
                    logger.trace("Input for Stage({}) : batchSize={}, inputRate={}, throughputRate={}, realThrouputRate={}",
                            new Object[]{getStage().getName(), batchSize,
                                    inputRateHelper.getRate(), throughputInfo.getRate(), throughputInfo.getRealRate()});
                }

                adjust();
            }

        } finally {
            lock.unlock();
        }
    }

    public double getInputRate() {
        return inputRateHelper.getRate();
    }

    public double getThroughputRate() {
        ThroughputInfo throughputInfo = findThroughputInfo.get(batchSize);
        return throughputInfo != null ? throughputInfo.getRate() : 0;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public int getMaxThreadCount() {
        return maxThreadCount;
    }

    public void setMaxThreadCount(int maxThreadCount) {
        this.maxThreadCount = maxThreadCount;
    }

    public void setMaxElementCount(int maxElementCount) {
        this.maxElementCount = maxElementCount;
    }

    public void setAdjustmentCount(int adjustCount) {
        this.adjustInfo.setCount(adjustCount);
    }

    public void setAdjustmentInterval(int adjustInterval) {
        this.adjustInfo.setInterval(adjustInterval);
    }

    public long getWaitForFillingTimeout() {
        return waitForFillingTimeout;
    }

    public void setWaitForFillingTimeout(long waitForFillingTimeout) {
        this.waitForFillingTimeout = waitForFillingTimeout;
    }

    private void makeAdjustment(ThroughputInfo trhoughputInfo) {
        if (logger.isTraceEnabled()) {
            logger.trace("Need adjustment for Stage({})", getStage().getName());
        }
        resort();
        ThroughputInfo best = findBest();
        if (logger.isTraceEnabled()) {
            logger.trace("Current best throughput for Stage({}) : batchSize={}, throuputRate={}",
                    new Object[]{getStage().getName(), best.getBatchSize(), best.getRate()});
        }
        ThroughputInfo result = null;
        if (needToAdjust(best)) {
            if (logger.isTraceEnabled()) {
                logger.trace("Need to find out better for Stage({}) than batchSize={}, throuputRate={}",
                        new Object[]{getStage().getName(), best.getBatchSize(), best.getRate()});
            }
            if (best.isLast()) {
                if (best.getBatchSize() < maxBatchSize) {
                    result = insertAfter(best);
                } else if (logger.isTraceEnabled()) {
                    logger.trace("Max batchSize for Stage({}) reached already",
                            new Object[]{getStage().getName(), best.getBatchSize(), best.getRate()});
                }
            } else if (best.isFirst()) {
                result = insertBefore(best);
            } else if (best.getRate() <= best.getNext().getRate()) {
                result = insertBetween(best, best.getNext());
            } else if (best.getRate() >= best.getPrev().getRate()) {
                result = insertBetween(best.getPrev(), best);
            }

            if (result != null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Insert new batchSize info for Stage({}) , batchSize={}",
                            new Object[]{getStage().getName(), result.getBatchSize()});
                }

                this.batchSize = result.getBatchSize();
            } else {
                this.batchSize = best.getBatchSize();
            }
        } else {
            this.batchSize = best.getBatchSize();
        }
    }

    private ThroughputInfo findBest() {
        return throughputInfoList.get(0);
    }

    private ThroughputInfo insertAfter(ThroughputInfo best) {
        int newBatchSize = best.getBatchSize();
        double throughputRate = best.getRate();
        int delta;

        // detect kickdown mode: when input increase high
        if (Math.max(getInputRate(), 1) / Math.max(throughputRate, 1) > 1.5) {
            delta = (int) (newBatchSize * (Math.max(getInputRate(), 1) / Math.max(throughputRate, 1)));
            if (logger.isTraceEnabled()) {
                logger.trace("Detect kick down for Stage({}) , delta={} ",
                        new Object[]{getStage().getName(), delta});
            }
        } else {
            delta = (int) (newBatchSize * INCREASE_BATCH_MULTIPLIER);
        }
        if (delta <= 0) {
            delta = 1;
        }

        newBatchSize += delta;

        if (newBatchSize > maxBatchSize) {
            newBatchSize = maxBatchSize;
            logger.trace("Reach for Stage({}) reached already",
                    new Object[]{getStage().getName(), best.getBatchSize(), best.getRate()});
        }

        ThroughputInfo result = new ThroughputInfo(newBatchSize, adjustInfo, best.getRate(),
                best.getRealRate(), best, null);
        best.setNext(result);

        findThroughputInfo.put(newBatchSize, result);
        throughputInfoList.add(result);

        adjustThroughputInfoList();

        return result;
    }

    private void adjustThroughputInfoList() {
        if (throughputInfoList.size() > maxElementCount) {
            resort();
            int count = throughputInfoList.size() - maxElementCount;
            for (int i = 0; i < count; i++) {
                ThroughputInfo remove = throughputInfoList.remove(throughputInfoList.size() - 1);
                findThroughputInfo.remove(remove.getBatchSize());

                if (!remove.isFirst()) {
                    remove.getPrev().setNext(remove.getNext());
                }

                if (!remove.isLast()) {
                    remove.getNext().setPrev(remove.getPrev());
                }

            }
        }
    }

    private ThroughputInfo insertBefore(ThroughputInfo best) {
        if (best.getBatchSize() == 1) {
            return null;
        }
        int newBatchSize = best.getBatchSize();
        int delta = (int) (newBatchSize * INCREASE_BATCH_MULTIPLIER);
        if (delta <= 0) {
            delta = 1;
        }

        newBatchSize -= delta;

        if (newBatchSize <= 0) {
            return null;
        }

        ThroughputInfo result = new ThroughputInfo(newBatchSize, adjustInfo, best.getRate(),
                best.getRealRate(), null, best);
        best.setPrev(result);
        findThroughputInfo.put(newBatchSize, result);
        throughputInfoList.add(result);

        adjustThroughputInfoList();

        return result;
    }

    private ThroughputInfo insertBetween(ThroughputInfo first, ThroughputInfo second) {
        int newBatchSize = ((first.getBatchSize() + second.getBatchSize()) / 2);
        if (newBatchSize == first.getBatchSize() || newBatchSize == second.getBatchSize()) {
            return null;
        }

        ThroughputInfo result = new ThroughputInfo(newBatchSize, adjustInfo,
                Math.max(first.getRate(), second.getRate()),
                Math.max(second.getRealRate(), second.getRealRate()), first, second);

        first.setNext(result);
        second.setPrev(result);

        findThroughputInfo.put(newBatchSize, result);
        throughputInfoList.add(result);

        adjustThroughputInfoList();

        return result;
    }

    private boolean needToAdjust(ThroughputInfo throughputInfo) {
        return throughputInfo.isReady() && inputRateHelper.getRate() > throughputInfo.getRate();
    }

    public void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }

    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    public long getProcessingTimeThreshold() {
        return processingTimeThreshold;
    }

    public void setProcessingTimeThreshold(long processingTimeThreshold) {
        this.processingTimeThreshold = processingTimeThreshold;
    }

    private void resort() {
        Collections.sort(throughputInfoList, ThroughputInfo.COMPARATOR);
    }
}
