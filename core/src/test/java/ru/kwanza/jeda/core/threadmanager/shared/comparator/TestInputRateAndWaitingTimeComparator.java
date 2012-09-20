package ru.kwanza.jeda.core.threadmanager.shared.comparator;

import junit.framework.TestCase;

import java.util.Comparator;

/**
 * @author Guzanov Alexander
 */
public class TestInputRateAndWaitingTimeComparator extends TestCase {
    public void test1() {
        InputRateAndWaitingTimeComparator comparator = new InputRateAndWaitingTimeComparator();
        comparator.setMaxWaitingTime(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, comparator.getMaxWaitingTime());
        TestStageEntry entry1 = new TestStageEntry();
        entry1.setTs(1);
        entry1.getStage().setInputRate(1.0d);

        TestStageEntry entry2 = new TestStageEntry();
        entry2.setTs(2);
        entry2.getStage().setInputRate(1.01d);


        assertEquals(1, comparator.compare(entry1, entry2));
        assertEquals(-1, comparator.compare(entry2, entry1));
    }

    public void test2() {
        InputRateAndWaitingTimeComparator comparator = new InputRateAndWaitingTimeComparator();
        comparator.setMaxWaitingTime(Long.MAX_VALUE);
        TestStageEntry entry1 = new TestStageEntry();
        entry1.setTs(1);
        entry1.getStage().setInputRate(1.01d);

        TestStageEntry entry2 = new TestStageEntry();
        entry2.setTs(2);
        entry2.getStage().setInputRate(1.01d);


        assertEquals(-1, comparator.compare(entry1, entry2));
        assertEquals(1, comparator.compare(entry2, entry1));
    }

    public void test3() {
        InputRateAndWaitingTimeComparator comparator = new InputRateAndWaitingTimeComparator();
        comparator.setMaxWaitingTime(Long.MAX_VALUE);
        TestStageEntry entry1 = new TestStageEntry();
        entry1.setTs(1);
        entry1.getStage().setInputRate(1.01d);

        TestStageEntry entry2 = new TestStageEntry();
        entry2.setTs(1);
        entry2.getStage().setInputRate(1.01d);


        assertEquals(0, comparator.compare(entry1, entry2));
        assertEquals(0, comparator.compare(entry2, entry1));
    }

    public void test4() throws InterruptedException {
        InputRateAndWaitingTimeComparator comparator = new InputRateAndWaitingTimeComparator();
        comparator.setMaxWaitingTime(10 * 1000);
        assertEquals(10 * 1000, comparator.getMaxWaitingTime());
        TestStageEntry entry1 = new TestStageEntry();
        entry1.setTs(System.currentTimeMillis());
        entry1.getStage().setInputRate(1.00d);
        Thread.currentThread().sleep(1000 * 8);
        TestStageEntry entry2 = new TestStageEntry();
        entry2.setTs(System.currentTimeMillis());
        entry2.getStage().setInputRate(1.01d);

        Thread.currentThread().sleep(3 * 1000);
        assertEquals(-1, comparator.compare(entry1, entry2));
        assertEquals(1, comparator.compare(entry2, entry1));
    }

    public void test5() {
        Comparator comparator = new InputRateAndWaitingTimeComparator();
        ((InputRateAndWaitingTimeComparator) comparator).setMaxWaitingTime(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, ((InputRateAndWaitingTimeComparator) comparator).getMaxWaitingTime());
        TestStageEntry entry1 = new TestStageEntry();
        entry1.setTs(1);
        entry1.getStage().setInputRate(1.01d);

        TestStageEntry entry2 = new TestStageEntry();
        entry2.setTs(2);
        entry2.getStage().setInputRate(1.01d);


        assertEquals(-1, comparator.compare(entry1, entry2));
        assertEquals(1, comparator.compare(entry2, entry1));
    }
}
