package ru.kwanza.jeda.timerservice.pushtimer;

import ru.kwanza.jeda.api.pushtimer.manager.TimerHandle;

/**
 * @author Michael Yeskov
 */
public class TimerEntity extends TimerHandle{

    private TimerState state;
    private Long bucketId;
    private Long expireTime;
    private long creationPointCount;

    public TimerEntity(String timerName, String timerId, Long expireTime) {
        super(timerName, timerId);
        this.expireTime = expireTime;
    }



    public TimerEntity(TimerHandle timerHandle, TimerState state, Long bucketId, Long expireTime, long creationPointCount) {
        super(timerHandle.getTimerName(), timerHandle.getTimerId());
        this.state = state;
        this.bucketId = bucketId;
        this.expireTime = expireTime;
        this.creationPointCount = creationPointCount;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public TimerState getState() {
        return state;
    }

    public void setState(TimerState state) {
        this.state = state;
    }

    public long getCreationPointCount() {
        return creationPointCount;
    }

    public void setCreationPointCount(long creationPointCount) {
        this.creationPointCount = creationPointCount;
    }

    public Long getBucketId() {
        return bucketId;
    }

    public void setBucketId(Long bucketId) {
        this.bucketId = bucketId;
    }
}
