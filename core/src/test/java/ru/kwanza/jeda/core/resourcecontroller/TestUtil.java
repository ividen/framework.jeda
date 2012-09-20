package ru.kwanza.jeda.core.resourcecontroller;

import junit.framework.TestCase;

/**
 * @author Guzanov Alexander
 */
public class TestUtil extends TestCase {

    public void test_smooth_average() {
        assertTrue(Math.abs(1.35 - Util.smooth_average(1.5, 1, 0.7)) < 0.0000000000001);
        assertTrue(Math.abs(1.245 - Util.smooth_average(1.2, 1.35, 0.7)) < 0.0000000000001);
    }

    public void testCompare() {
        assertEquals(-1, Util.compare(0.123, 0.125, 0.01));
        assertEquals(0, Util.compare(0.123, 0.125, 0.017));

        assertEquals(1, Util.compare(0.125, 0.123, 0.01));
        assertEquals(0, Util.compare(0.125, 0.123, 0.017));
    }
}
