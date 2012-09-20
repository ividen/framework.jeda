package ru.kwanza.jeda.core.resourcecontroller;

/**
 * @author Guzanov Alexander
 */
class RateAdjustInfo {
    private int count;
    private int interval;
    private int iterationCount;

    RateAdjustInfo(int count, int interval) {
        this(count, interval, 0);
    }

    RateAdjustInfo(int count, int interval, int iterationCount) {
        this.count = count;
        this.interval = interval;
        this.iterationCount = iterationCount;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getIterationCount() {
        return iterationCount;
    }

    public void setIterationCount(int iterationCount) {
        this.iterationCount = iterationCount;
    }
}
