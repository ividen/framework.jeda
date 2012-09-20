package ru.kwanza.jeda.core.threadmanager.shared.comparator;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.IEventProcessor;
import ru.kwanza.jeda.api.IFlowBus;
import ru.kwanza.jeda.api.ISink;
import ru.kwanza.jeda.api.internal.*;

/**
 * @author Guzanov Alexander
 */
public class TestStage implements IStageInternal {
    private double inputRate;
    private double throughputRate;
    private int batchSize;
    private int threadCount;

    private TestResourceController resourceController = new TestResourceController();

    public final class TestResourceController implements IResourceController {
        public void throughput(int count, int batchSize, long millis, boolean success) {

        }

        public void input(long count) {

        }

        public double getInputRate() {
            return inputRate;
        }

        public double getThroughputRate() {
            return throughputRate;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public int getThreadCount() {
            return threadCount;
        }
    }

    public String getName() {
        return null;
    }

    public <E extends IEvent> ISink<E> getSink() {
        return null;
    }

    public IThreadManager getThreadManager() {
        return null;
    }

    public IQueue getQueue() {
        return null;
    }

    public IAdmissionController getAdmissionController() {
        return null;
    }

    public IFlowBus getFlowBus() {
        return null;
    }

    public IEventProcessor getProcessor() {
        return null;
    }

    public boolean hasTransaction() {
        return false;
    }

    public IResourceController getResourceController() {
        return resourceController;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public double getInputRate() {
        return inputRate;
    }

    public void setInputRate(double inputRate) {
        this.inputRate = inputRate;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public double getThroughputRate() {
        return throughputRate;
    }

    public void setThroughputRate(double throughputRate) {
        this.throughputRate = throughputRate;
    }
}
