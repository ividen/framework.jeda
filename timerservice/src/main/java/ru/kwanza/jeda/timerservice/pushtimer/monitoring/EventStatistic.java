package ru.kwanza.jeda.timerservice.pushtimer.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Michael Yeskov
 */
public class EventStatistic {
    private static final Logger logger = LoggerFactory.getLogger(EventStatistic.class);

    AtomicLong count = new AtomicLong(0);

    private volatile long totalStart;
    private volatile double totalTimeSec;
    private volatile long totalCount;
    private volatile double totalThroughput;

    private volatile long intervalStart;
    private volatile long intervalStartCount;

    private volatile double intervalTimeSec;
    private volatile long intervalCount;
    private volatile double intervalThroughput;

    private EventStatisticsThread thread;


    private volatile long interval = 10000;
    private String name;

    public EventStatistic(String name) {
        this.name = name;
    }

    @PostConstruct
    public void init(){
        totalStart = System.currentTimeMillis();
        thread = new EventStatisticsThread();
        thread.start();
    }

    public void registerEvents (long eventCount) {
        count.addAndGet(eventCount);
    }


    private class EventStatisticsThread extends Thread{

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    intervalStart = System.currentTimeMillis();
                    intervalStartCount = count.get();
                    Thread.sleep(interval);

                    long now  = System.currentTimeMillis();
                    long currentCount = count.get();

                    totalTimeSec = (now - totalStart) / 1000.0;
                    totalCount = currentCount;
                    totalThroughput = 0;
                    if (totalTimeSec > 0) {
                        totalThroughput = ((double)totalCount)  / totalTimeSec;
                    }

                    intervalTimeSec = (now - intervalStart) / 1000.0;
                    intervalCount = currentCount - intervalStartCount;
                    if (intervalTimeSec > 0) {
                        intervalThroughput = ((double)intervalCount)  / intervalTimeSec;
                    }

                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

    }

    public void resetTotal() {
        totalStart = System.currentTimeMillis();
        count.set(0);
    }

    public double getTotalTimeSec() {
        return totalTimeSec;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public double getTotalThroughput() {
        return totalThroughput;
    }

    public long getTotalStart() {
        return totalStart;
    }


    public double getIntervalTimeSec() {
        return intervalTimeSec;
    }

    public long getIntervalCount() {
        return intervalCount;
    }

    public double getIntervalThroughput() {
        return intervalThroughput;
    }

    public long getIntervalStart() {
        return intervalStart;
    }


    //config
    public String getName() {
        return name;
    }
    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }
}
