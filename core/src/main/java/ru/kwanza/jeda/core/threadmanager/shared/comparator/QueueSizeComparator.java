package ru.kwanza.jeda.core.threadmanager.shared.comparator;

import ru.kwanza.jeda.core.threadmanager.shared.StageEntry;

import java.util.Comparator;

/**
 * @author Guzanov Alexander
 */
public class QueueSizeComparator implements Comparator<StageEntry> {
    public int compare(StageEntry o1, StageEntry o2) {
        return o2.getStage().getQueue().getEstimatedCount() - o1.getStage().getQueue().getEstimatedCount();
    }
}
