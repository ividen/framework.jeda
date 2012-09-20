package ru.kwanza.jeda.nio.client;

import ru.kwanza.jeda.core.threadmanager.shared.StageEntry;

import java.util.Comparator;

/**
 * @author Guzanov Alexander
 */
class WaitingForConnectComparator implements Comparator<StageEntry> {
    private long maxWaitingTime = 60 * 1000;

    public int compare(StageEntry o1, StageEntry o2) {
        long size1 = o1.getStage().getQueue().getEstimatedCount();
        long size2 = o1.getStage().getQueue().getEstimatedCount();

        long batchSize1 = (o1.getStage().getResourceController()).getBatchSize();
        long batchSize2 = (o1.getStage().getResourceController()).getBatchSize();

        long processingCount1 = Math.min(size1, batchSize1);
        long processingCount2 = Math.min(size2, batchSize2);

        long currentTs = System.currentTimeMillis();

        long interval1 = currentTs - o1.getTs();
        long interval2 = currentTs - o2.getTs();

        if ((interval1 < maxWaitingTime && interval2 < maxWaitingTime) ||
                (interval1 > maxWaitingTime && interval2 > maxWaitingTime)) {
            if (processingCount1 > processingCount2) {
                return -1;
            }

            if (processingCount1 < processingCount2) {
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
