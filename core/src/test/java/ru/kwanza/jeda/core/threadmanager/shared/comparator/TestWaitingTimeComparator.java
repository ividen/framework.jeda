package ru.kwanza.jeda.core.threadmanager.shared.comparator;

import org.junit.Test;

import java.util.Comparator;

import static junit.framework.Assert.assertEquals;

/**
 * @author Guzanov Alexander
 */
public class TestWaitingTimeComparator{
    @Test
    public void test1() {
        WaitingTimeComparator comparator = new WaitingTimeComparator();
        TestStageEntry entry1 = new TestStageEntry();
        entry1.setTs(1);

        TestStageEntry entry2 = new TestStageEntry();
        entry2.setTs(2);


        assertEquals(-1, comparator.compare(entry1, entry2));
        assertEquals(1, comparator.compare(entry2, entry1));
    }

    @Test
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
