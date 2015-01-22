package ru.kwanza.jeda.timerservice.memorytimer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.helper.FlushResult;
import ru.kwanza.jeda.api.helper.SinkHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.DelayQueue;

/**
 * @author Michael Yeskov
 */
public class TimeoutProcessor extends Thread {
    private static final Logger log = LoggerFactory.getLogger(TimeoutProcessor.class);

    private volatile boolean active = true;
    private int maxBatchSize;
    private long sleepTime;
    private IJedaManager jedaManager;

    private IDeclineProcessor declineProcessor;

    private DelayQueue<DelayedEvent> queue = new DelayQueue<DelayedEvent>();


    public TimeoutProcessor(int maxBatchSize, long sleepTime, IDeclineProcessor declineProcessor, IJedaManager jedaManager) {
        super("MemoryTimerProcessor");
        this.maxBatchSize = maxBatchSize;
        this.sleepTime = sleepTime;
        this.declineProcessor = declineProcessor;
        this.jedaManager = jedaManager;
    }

    @Override
    public void run() {
        Collection<DelayedEvent> fired = new ArrayList<>(maxBatchSize);
        while (active && !isInterrupted()) {
            try {
                fired.clear();
                queue.drainTo(fired, maxBatchSize);
                if (fired.size() > 0) {
                    process(fired);
                }
                if (fired.size() < maxBatchSize) {
                    Thread.sleep(sleepTime);
                }
            } catch (InterruptedException e) {
                active = false;
            } catch (Throwable e) {
                log.error("Exception in TimeoutProcessor thread", e);
            }
        }
    }

    private void process(Collection<DelayedEvent> fired) {
        SinkHelper sinkHelper = new SinkHelper(jedaManager);
        for (DelayedEvent current : fired){
            sinkHelper.put(current.getDestinationStageName(), current.getEvent());
        }

        if (declineProcessor == null) {
            sinkHelper.flushOrSuspend();
        } else {
            SinkHelper responses = new SinkHelper(jedaManager);
            declineProcessor.processFlushResult(sinkHelper.flush(), responses);
            responses.flushOrSuspend();
        }

    }

    public void add(IEvent event, String destinationStageName, long fireTime) {
        queue.add(new DelayedEvent(event, destinationStageName, fireTime));
    }

    public void stopProcessing() {
        active = false;
    }
}
