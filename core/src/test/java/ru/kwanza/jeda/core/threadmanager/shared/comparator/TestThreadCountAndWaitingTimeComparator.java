package ru.kwanza.jeda.core.threadmanager.shared.comparator;

import org.junit.Test;

import java.util.Comparator;

import static junit.framework.Assert.assertEquals;

/**
 * @author Guzanov Alexander
 */
public class TestThreadCountAndWaitingTimeComparator{
    @Test
    public void test1() {
        ThreadCountAndWaitingTimeComparator comparator = new ThreadCountAndWaitingTimeComparator();
        comparator.setMaxWaitingTime(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, comparator.getMaxWaitingTime());
        TestStageEntry entry1 = new TestStageEntry();
        entry1.setTs(1);
        entry1.setThreadCount(1);

        TestStageEntry entry2 = new TestStageEntry();
        entry2.setTs(2);
        entry2.setThreadCount(2);


        assertEquals(1, comparator.compare(entry1, entry2));
        assertEquals(-1, comparator.compare(entry2, entry1));
    }

    @Test
    public void test2() {
        ThreadCountAndWaitingTimeComparator comparator = new ThreadCountAndWaitingTimeComparator();
        comparator.setMaxWaitingTime(Long.MAX_VALUE);
        TestStageEntry entry1 = new TestStageEntry();
        entry1.setTs(1);
        entry1.setThreadCount(2);

        TestStageEntry entry2 = new TestStageEntry();
        entry2.setTs(2);
        entry2.setThreadCount(1);


        assertEquals(-1, comparator.compare(entry1, entry2));
        assertEquals(1, comparator.compare(entry2, entry1));
    }

    @Test
    public void test3() {
        ThreadCountAndWaitingTimeComparator comparator = new ThreadCountAndWaitingTimeComparator();
        comparator.setMaxWaitingTime(Long.MAX_VALUE);
        TestStageEntry entry1 = new TestStageEntry();
        entry1.setTs(1);
        entry1.setThreadCount(2);

        TestStageEntry entry2 = new TestStageEntry();
        entry2.setTs(1);
        entry2.setThreadCount(2);


        assertEquals(0, comparator.compare(entry1, entry2));
        assertEquals(0, comparator.compare(entry2, entry1));
    }

    @Test
    public void test4() throws InterruptedException {
        ThreadCountAndWaitingTimeComparator comparator = new ThreadCountAndWaitingTimeComparator();
        comparator.setMaxWaitingTime(10 * 1000);
        assertEquals(10 * 1000, comparator.getMaxWaitingTime());
        TestStageEntry entry1 = new TestStageEntry();
        entry1.setTs(System.currentTimeMillis());
        entry1.setThreadCount(1);
        Thread.currentThread().sleep(1000 * 8);
        TestStageEntry entry2 = new TestStageEntry();
        entry2.setTs(System.currentTimeMillis());
        entry2.setThreadCount(2);

        Thread.currentThread().sleep(3 * 1000);
        assertEquals(-1, comparator.compare(entry1, entry2));
        assertEquals(1, comparator.compare(entry2, entry1));
    }

    @Test
    public void test5() {
        Comparator comparator = new ThreadCountAndWaitingTimeComparator();
        ((ThreadCountAndWaitingTimeComparator) comparator).setMaxWaitingTime(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, ((ThreadCountAndWaitingTimeComparator) comparator).getMaxWaitingTime());
        TestStageEntry entry1 = new TestStageEntry();
        entry1.setTs(1);
        entry1.setThreadCount(2);

        TestStageEntry entry2 = new TestStageEntry();
        entry2.setTs(2);
        entry2.setThreadCount(2);


        assertEquals(-1, comparator.compare(entry1, entry2));
        assertEquals(1, comparator.compare(entry2, entry1));
    }
}
