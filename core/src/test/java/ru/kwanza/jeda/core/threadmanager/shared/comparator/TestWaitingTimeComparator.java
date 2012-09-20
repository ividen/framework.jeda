package ru.kwanza.jeda.core.threadmanager.shared.comparator;

import junit.framework.TestCase;

import java.util.Comparator;

/**
 * @author Guzanov Alexander
 */
public class TestWaitingTimeComparator extends TestCase {
    public void test1() {
        WaitingTimeComparator comparator = new WaitingTimeComparator();
        TestStageEntry entry1 = new TestStageEntry();
        entry1.setTs(1);

        TestStageEntry entry2 = new TestStageEntry();
        entry2.setTs(2);


        assertEquals(-1, comparator.compare(entry1, entry2));
        assertEquals(1, comparator.compare(entry2, entry1));
    }

    public void test2() {
        Comparator comparator = new WaitingTimeComparator();
        TestStageEntry entry1 = new TestStageEntry();
        entry1.setTs(1);

        TestStageEntry entry2 = new TestStageEntry();
        entry2.setTs(1);


        assertEquals(0, comparator.compare(entry1, entry2));
        assertEquals(0, comparator.compare(entry2, entry1));
    }
}
