package ru.kwanza.jeda.core.threadmanager.shared.comparator;

import ru.kwanza.jeda.core.threadmanager.shared.StageEntry;

import java.util.Comparator;

/**
 * @author Guzanov Alexander
 */
public class RoundRobinComparator implements Comparator<StageEntry> {
    public int compare(StageEntry o1, StageEntry o2) {
        long l = o1.getTs() - o2.getTs();
        return l < 0 ? -1 : (l > 0 ? 1 : 0);
    }
}