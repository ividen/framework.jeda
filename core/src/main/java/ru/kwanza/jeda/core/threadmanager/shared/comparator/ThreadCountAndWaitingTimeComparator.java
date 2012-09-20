package ru.kwanza.jeda.core.threadmanager.shared.comparator;

import ru.kwanza.jeda.core.threadmanager.shared.StageEntry;

import java.util.Comparator;

/**
 * @author Guzanov Alexander
 */
public class ThreadCountAndWaitingTimeComparator implements Comparator<StageEntry> {
    private long maxWaitingTime = 5 * 60 * 1000;

    public ThreadCountAndWaitingTimeComparator() {
    }

    public int compare(StageEntry o1, StageEntry o2) {
        double threadCount1 = o1.getThreadCount();
        double threadCount2 = o2.getThreadCount();

        long currentTs = System.currentTimeMillis();

        long interval1 = currentTs - o1.getTs();
        long interval2 = currentTs - o2.getTs();

        if ((interval1 < maxWaitingTime && interval2 < maxWaitingTime) ||
                (interval1 > maxWaitingTime && interval2 > maxWaitingTime)) {
            if (threadCount1 > threadCount2) {
                return -1;
            }

            if (threadCount1 < threadCount2) {
                return 1;
            }

            if (interval1 > interval2) {
                return -1;
            }

            if (interval1 < interval2) {
                return 1;
            }

            return 0;
        } else if (interval1 > maxWaitingTime) {
            return -1;
        } else {
            return 1;
        }
    }

    public long getMaxWaitingTime() {
        return maxWaitingTime;
    }

    public void setMaxWaitingTime(long maxWaitingTime) {
        this.maxWaitingTime = maxWaitingTime;
    }
}