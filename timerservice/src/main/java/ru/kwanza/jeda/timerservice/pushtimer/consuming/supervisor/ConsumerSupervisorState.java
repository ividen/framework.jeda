package ru.kwanza.jeda.timerservice.pushtimer.consuming.supervisor;

/**
 * @author Michael Yeskov
 */
public class ConsumerSupervisorState {

    private long oldestTimerInMemory;  //>=
    private long consumerLeftBorder;   //>=
    private long consumerRightBorder;  //<=


    public ConsumerSupervisorState(long oldestTimerInMemory, long consumerLeftBorder, long consumerRightBorder, long availableRightBorder) {
        this.oldestTimerInMemory = oldestTimerInMemory;
        this.consumerLeftBorder = consumerLeftBorder;
        this.consumerRightBorder = consumerRightBorder;
    }

    public long getOldestTimerInMemory() {
        return oldestTimerInMemory;
    }

    public void setOldestTimerInMemory(long oldestTimerInMemory) {
        this.oldestTimerInMemory = oldestTimerInMemory;
    }

    public long getConsumerLeftBorder() {
        return consumerLeftBorder;
    }

    public void setConsumerLeftBorder(long consumerLeftBorder) {
        this.consumerLeftBorder = consumerLeftBorder;
    }

    public long getConsumerRightBorder() {
        return consumerRightBorder;
    }

    public void setConsumerRightBorder(long consumerRightBorder) {
        this.consumerRightBorder = consumerRightBorder;
    }

}
