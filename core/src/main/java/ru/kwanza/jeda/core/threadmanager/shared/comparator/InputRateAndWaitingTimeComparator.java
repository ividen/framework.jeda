package ru.kwanza.jeda.core.threadmanager.shared.comparator;

import ru.kwanza.jeda.core.threadmanager.shared.StageEntry;

import java.util.Comparator;

/**
 * @author Guzanov Alexander
 */
public class InputRateAndWaitingTimeComparator implements Comparator<StageEntry> {
    private long maxWaitingTime = 5 * 60 * 1000;

    public InputRateAndWaitingTimeComparator() {
    }

    public int compare(StageEntry o1, StageEntry o2) {
        double inputRate1 = o1.getStage().getResourceController().getInputRate();
        double inputRate2 = o2.getStage().getResourceController().getInputRate();

        long currentTs = System.currentTimeMillis();

        long interval1 = currentTs - o1.getTs();
        long interval2 = currentTs - o2.getTs();

        if ((interval1 < maxWaitingTime && interval2 < maxWaitingTime) ||
                (interval1 > maxWaitingTime && interval2 > maxWaitingTime)) {
            if (inputRate1 > inputRate2) {
                return -1;
            }

            if (inputRate2 > inputRate1) {
                return 1;
            }

            if (interval2 < interval1) {
                return -1;
            }

            if (interval2 > interval1) {
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