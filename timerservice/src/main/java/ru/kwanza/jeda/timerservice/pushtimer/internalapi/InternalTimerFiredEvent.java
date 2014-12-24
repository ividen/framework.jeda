package ru.kwanza.jeda.timerservice.pushtimer.internalapi;

import ru.kwanza.jeda.api.pushtimer.TimerFiredEvent;

/**
 * @author Michael Yeskov
 */
public class InternalTimerFiredEvent extends TimerFiredEvent {

    private long bucketId;
    private long expireTime;
    private long nodeStartupPointCount;

    public InternalTimerFiredEvent(String timerName, String timerId, long bucketId, long expireTime, long nodeStartupPointCount) {
        super(timerName,timerId);
        this.bucketId = bucketId;
        this.expireTime = expireTime;
        this.nodeStartupPointCount = nodeStartupPointCount;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public long getBucketId() {
        return bucketId;
    }

    public long getNodeStartupPointCount() {
        return nodeStartupPointCount;
    }
}
