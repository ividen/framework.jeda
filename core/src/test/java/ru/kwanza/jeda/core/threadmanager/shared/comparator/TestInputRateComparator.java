package ru.kwanza.jeda.core.threadmanager.shared.comparator;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * @author Guzanov Alexander
 */
public class TestInputRateComparator{
    @Test
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

    @Test
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
