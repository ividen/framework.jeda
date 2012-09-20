package ru.kwanza.jeda.core.threadmanager.shared.comparator;

import junit.framework.TestCase;

/**
 * @author Guzanov Alexander
 */
public class TestThreadCountComparator extends TestCase {
    public void test1() {
        ThreadCountComparator comparator = new ThreadCountComparator();
        TestStageEntry entry1 = new TestStageEntry();
        entry1.setTs(1);
        entry1.setThreadCount(1);

        TestStageEntry entry2 = new TestStageEntry();
        entry2.setTs(2);
        entry2.setThreadCount(2);


        assertEquals(1, comparator.compare(entry1, entry2));
        assertEquals(-1, comparator.compare(entry2, entry1));
    }

    public void test2() {
        ThreadCountComparator comparator = new ThreadCountComparator();
        TestStageEntry entry1 = new TestStageEntry();
        entry1.setTs(1);
        entry1.setThreadCount(4);

        TestStageEntry entry2 = new TestStageEntry();
        entry2.setTs(2);
        entry2.setThreadCount(4);


        assertEquals(0, comparator.compare(entry1, entry2));
        assertEquals(0, comparator.compare(entry2, entry1));
    }
}
