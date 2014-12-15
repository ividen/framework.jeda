package ru.kwanza.jeda.timerservice.pushtimer.consuming;

/**
 * @author Michael Yeskov
 */
public class ConsumerConfig {
    //TODO: check that config in range

    private int workerCount = 6;
    private long borderGain = 10000;
    private long idealWorkingInterval = 300000;

    //FiredTimersMemoryStorage
    private long firedTimersMaxLimit = 1000000;
    private long firedTimersSingleConsumerModeLimit = 800000;
    private long firedTimersAgainMultiConsumerBorder = 300000;



    public int getWorkerCount() {
        return workerCount;
    }

    public void setWorkerCount(int workerCount) {
        this.workerCount = workerCount;
    }

    public long getBorderGain() {
        return borderGain;
    }

    public void setBorderGain(long borderGain) {
        this.borderGain = borderGain;
    }

    public long getFiredTimersMaxLimit() {
        return firedTimersMaxLimit;
    }

    public void setFiredTimersMaxLimit(long firedTimersMaxLimit) {
        this.firedTimersMaxLimit = firedTimersMaxLimit;
    }

    public long getFiredTimersSingleConsumerModeLimit() {
        return firedTimersSingleConsumerModeLimit;
    }

    public void setFiredTimersSingleConsumerModeLimit(long firedTimersSingleConsumerModeLimit) {
        this.firedTimersSingleConsumerModeLimit = firedTimersSingleConsumerModeLimit;
    }

    public long getFiredTimersAgainMultiConsumerBorder() {
        return firedTimersAgainMultiConsumerBorder;
    }

    public void setFiredTimersAgainMultiConsumerBorder(long firedTimersAgainMultiConsumerBorder) {
        this.firedTimersAgainMultiConsumerBorder = firedTimersAgainMultiConsumerBorder;
    }

    public long getIdealWorkingInterval() {
        return idealWorkingInterval;
    }

    public void setIdealWorkingInterval(long idealWorkingInterval) {
        this.idealWorkingInterval = idealWorkingInterval;
    }

    @Override
    public String toString() {
        return "ConsumerConfig{" +
                "workerCount=" + workerCount +
                ", borderGain=" + borderGain +
                ", idealWorkingInterval=" + idealWorkingInterval +
                ", firedTimersMaxLimit=" + firedTimersMaxLimit +
                ", firedTimersSingleConsumerModeLimit=" + firedTimersSingleConsumerModeLimit +
                ", firedTimersAgainMultiConsumerBorder=" + firedTimersAgainMultiConsumerBorder +
                '}';
    }
}
