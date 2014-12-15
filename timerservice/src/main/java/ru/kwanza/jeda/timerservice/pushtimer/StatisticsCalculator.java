package ru.kwanza.jeda.timerservice.pushtimer;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Michael Yeskov
 */
public class StatisticsCalculator {
    AtomicLong count = new AtomicLong(0);
    private long firstStart;

    private long start;
    private long startCount;

    private String name;

    public StatisticsCalculator(String name) {
        this.name = name;
    }

    public void register (long eventCount) {
        count.addAndGet(eventCount);
    }

    public void firstStart(){
        firstStart = System.currentTimeMillis();
    }

    public void start(){
        start  = System.currentTimeMillis();
        startCount = count.get();
    }

    public void stopAndPrint() {
        long end = System.currentTimeMillis();
        long endCount = count.get();

        double  totalTimeSec = (end - firstStart) / 1000.0;
        double totalThroughput =  0;
        if (totalTimeSec > 0 ) {
            totalThroughput =  endCount / totalTimeSec;
        }

        long currentCount = endCount - startCount;
        double currentTimeSec = (end - start) / 1000.0;
        double currentThroughput = 0;
        if (currentTimeSec > 0) {
            currentThroughput = currentCount / currentTimeSec;
        }

        System.out.println("Statistics result '" + name + "'");
        System.out.println(String.format("totalTimeSec: %.4f totalCount %d totalThroughput %.4f", totalTimeSec, endCount, totalThroughput));
        System.out.println(String.format("currentTimeSec: %.4f currentCount %d currentThroughput %.4f", currentTimeSec, currentCount, currentThroughput));
        System.out.println("=================================");
    }


    public static StatisticsCalculator insert = new StatisticsCalculator("INSERT");
    public static StatisticsCalculator process = new StatisticsCalculator("PROCESS");

}
