package ru.kwanza.jeda.mock;

import junit.framework.TestSuite;

/**
 * @author Guzanov Alexander
 */
public class AllTests {

    public static TestSuite suite() {
        TestSuite mockTests = new TestSuite("MockTests");
        mockTests.addTest(new TestSuite(TestMocks.class));
        return mockTests;
    }
}
