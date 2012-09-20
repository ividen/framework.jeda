package ru.kwanza.jeda.core.threadmanager.shared.comparator;

import ru.kwanza.jeda.core.threadmanager.shared.StageEntry;

import java.util.Comparator;

/**
 * @author Guzanov Alexander
 */
public class WaitingTimeComparator implements Comparator<StageEntry> {
    public WaitingTimeComparator() {
    }

    public int compare(StageEntry o1, StageEntry o2) {
        long currentTs = System.currentTimeMillis();

        long interval1 = currentTs - o1.getTs();
        long interval2 = currentTs - o2.getTs();

        if (interval1 > interval2)
            return -1;
        if (interval1 < interval2)
            return 1;

        return 0;
    }
}