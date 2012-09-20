package ru.kwanza.jeda.core.threadmanager.shared.comparator;

import junit.framework.TestCase;

/**
 * @author Guzanov Alexander
 */
public class TestInputRateComparator extends TestCase {
    public void test1() {
        InputRateComparator comparator = new InputRateComparator();
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
        InputRateComparator comparator = new InputRateComparator();
        TestStageEntry entry1 = new TestStageEntry();
        entry1.setTs(1);
        entry1.getStage().setInputRate(1.0d);

        TestStageEntry entry2 = new TestStageEntry();
        entry2.setTs(2);
        entry2.getStage().setInputRate(1.00d);


        assertEquals(0, comparator.compare(entry1, entry2));
        assertEquals(0, comparator.compare(entry2, entry1));
    }
}
