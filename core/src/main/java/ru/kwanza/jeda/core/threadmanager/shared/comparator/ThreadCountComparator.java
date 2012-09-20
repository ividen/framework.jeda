package ru.kwanza.jeda.core.threadmanager.shared.comparator;

import ru.kwanza.jeda.core.threadmanager.shared.StageEntry;

import java.util.Comparator;

/**
 * @author Guzanov Alexander
 */
public class ThreadCountComparator implements Comparator<StageEntry> {
    public int compare(StageEntry o1, StageEntry o2) {
        return o2.getThreadCount() - o1.getThreadCount();
    }
}
