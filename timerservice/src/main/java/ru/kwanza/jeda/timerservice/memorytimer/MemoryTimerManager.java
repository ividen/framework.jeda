package ru.kwanza.jeda.timerservice.memorytimer;

import org.springframework.stereotype.Component;
import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.IJedaManager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Michael Yeskov
 */
public class MemoryTimerManager {

    private ConcurrentHashMap<String, AtomicLong> bucketIdToSize = new ConcurrentHashMap<String, AtomicLong>();
    private TimeoutProcessor timeoutProcessor;

    private int maxBatchSize = 1000;
    private long sleepTime = 500;
    private IDeclineProcessor declineProcessor = null;

    @Resource
    private IJedaManager jedaManager;

    @PostConstruct
    public void init(){
        timeoutProcessor = new TimeoutProcessor(maxBatchSize, sleepTime, declineProcessor, jedaManager);
        timeoutProcessor.start();
    }

    @PreDestroy
    public void stop(){
        timeoutProcessor.stopProcessing();
    }

    public boolean trySchedule(IEvent event, String destinationStageName, long fireTime, String bucketId, long maxBucketSize) {
        AtomicLong bucketSize = getBucketSize(bucketId);
        long newSize = bucketSize.incrementAndGet();
        if (newSize > maxBucketSize) {
            bucketSize.decrementAndGet();
            return false;
        }

        timeoutProcessor.add(event, destinationStageName, fireTime);
        return true;
    }

    private AtomicLong getBucketSize(String bucketId) {
        AtomicLong result = bucketIdToSize.get(bucketId);
        if (result == null) {
            result = new AtomicLong(0);
            AtomicLong oldValue = bucketIdToSize.putIfAbsent(bucketId, result);
            if (oldValue != null) {
                result = oldValue;
            }
        }
        return result;
    }

    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    public void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }

    public long getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    public IDeclineProcessor getDeclineProcessor() {
        return declineProcessor;
    }

    public void setDeclineProcessor(IDeclineProcessor declineProcessor) {
        this.declineProcessor = declineProcessor;
    }
}
