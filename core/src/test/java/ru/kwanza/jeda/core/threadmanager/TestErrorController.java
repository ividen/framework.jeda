package ru.kwanza.jeda.core.threadmanager;

import junit.framework.TestCase;

/**
 * @author Guzanov Alexander
 */
public class TestErrorController extends TestCase {
    public void testDangerousEntryEqual() {
        assertFalse(new ErrorController.DangerousEntry(new TestEvent("event1")).equals(new Object()));

        TestEvent event = new TestEvent("event1");
        ErrorController.DangerousEntry entry = new ErrorController.DangerousEntry(event);
        assertTrue(entry.equals(new ErrorController.DangerousEntry(event)));


        ErrorController.DangerousEntry entry2 = new ErrorController.DangerousEntry(new TestEvent("test"));
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        assertFalse(entry2.equals(entry));
        assertFalse(entry.equals(entry2));
    }

    public void testRegisterRemove() {
        TestStage stage1 = new TestStage("stage1");
        TestStage stage2 = new TestStage("stage2");
        TestEvent event1 = new TestEvent("event1");
        TestEvent event2 = new TestEvent("event2");
        TestEvent event3 = new TestEvent("event3");

        assertEquals("Wrong attempt count", 1, ErrorController.getInstance().registerDangerousElement(stage1, event1).getAttempts());
        assertEquals("Wrong attempt count", 2, ErrorController.getInstance().registerDangerousElement(stage1, event1).getAttempts());
        assertEquals("Wrong attempt count", 3, ErrorController.getInstance().registerDangerousElement(stage1, event1).getAttempts());
        assertEquals("Wrong attempt count", 4, ErrorController.getInstance().registerDangerousElement(stage1, event1).getAttempts());

        assertEquals("Wrong attempt count", 1, ErrorController.getInstance().registerDangerousElement(stage1, event2).getAttempts());
        assertEquals("Wrong attempt count", 2, ErrorController.getInstance().registerDangerousElement(stage1, event2).getAttempts());
        assertEquals("Wrong attempt count", 3, ErrorController.getInstance().registerDangerousElement(stage1, event2).getAttempts());
        assertEquals("Wrong attempt count", 4, ErrorController.getInstance().registerDangerousElement(stage1, event2).getAttempts());
        assertEquals("Wrong attempt count", 5, ErrorController.getInstance().registerDangerousElement(stage1, event2).getAttempts());

        assertEquals("Wrong attempt count", 1, ErrorController.getInstance().registerDangerousElement(stage1, event3).getAttempts());
        assertEquals("Wrong attempt count", 2, ErrorController.getInstance().registerDangerousElement(stage1, event3).getAttempts());
        assertEquals("Wrong attempt count", 3, ErrorController.getInstance().registerDangerousElement(stage1, event3).getAttempts());
        assertEquals("Wrong attempt count", 4, ErrorController.getInstance().registerDangerousElement(stage1, event3).getAttempts());
        assertEquals("Wrong attempt count", 5, ErrorController.getInstance().registerDangerousElement(stage1, event3).getAttempts());
        assertEquals("Wrong attempt count", 6, ErrorController.getInstance().registerDangerousElement(stage1, event3).getAttempts());

        assertEquals("Wrong attempt count", 4, ErrorController.getInstance().findDangerousElement(stage1, event1).getAttempts());
        assertEquals("Wrong attempt count", 5, ErrorController.getInstance().findDangerousElement(stage1, event2).getAttempts());
        assertEquals("Wrong attempt count", 6, ErrorController.getInstance().findDangerousElement(stage1, event3).getAttempts());


        assertEquals("Wrong attempt count", 1, ErrorController.getInstance().registerDangerousElement(stage2, event1).getAttempts());
        assertEquals("Wrong attempt count", 2, ErrorController.getInstance().registerDangerousElement(stage2, event1).getAttempts());
        assertEquals("Wrong attempt count", 3, ErrorController.getInstance().registerDangerousElement(stage2, event1).getAttempts());
        assertEquals("Wrong attempt count", 4, ErrorController.getInstance().registerDangerousElement(stage2, event1).getAttempts());

        assertEquals("Wrong attempt count", 1, ErrorController.getInstance().registerDangerousElement(stage2, event2).getAttempts());
        assertEquals("Wrong attempt count", 2, ErrorController.getInstance().registerDangerousElement(stage2, event2).getAttempts());
        assertEquals("Wrong attempt count", 3, ErrorController.getInstance().registerDangerousElement(stage2, event2).getAttempts());
        assertEquals("Wrong attempt count", 4, ErrorController.getInstance().registerDangerousElement(stage2, event2).getAttempts());
        assertEquals("Wrong attempt count", 5, ErrorController.getInstance().registerDangerousElement(stage2, event2).getAttempts());

        assertEquals("Wrong attempt count", 1, ErrorController.getInstance().registerDangerousElement(stage2, event3).getAttempts());
        assertEquals("Wrong attempt count", 2, ErrorController.getInstance().registerDangerousElement(stage2, event3).getAttempts());
        assertEquals("Wrong attempt count", 3, ErrorController.getInstance().registerDangerousElement(stage2, event3).getAttempts());
        assertEquals("Wrong attempt count", 4, ErrorController.getInstance().registerDangerousElement(stage2, event3).getAttempts());
        assertEquals("Wrong attempt count", 5, ErrorController.getInstance().registerDangerousElement(stage2, event3).getAttempts());
        assertEquals("Wrong attempt count", 6, ErrorController.getInstance().registerDangerousElement(stage2, event3).getAttempts());


        assertEquals("Wrong attempt count", 4, ErrorController.getInstance().findDangerousElement(stage2, event1).getAttempts());
        assertEquals("Wrong attempt count", 5, ErrorController.getInstance().findDangerousElement(stage2, event2).getAttempts());
        assertEquals("Wrong attempt count", 6, ErrorController.getInstance().findDangerousElement(stage2, event3).getAttempts());

        ErrorController.getInstance().removeDangerousElement(stage1, event1);
        ErrorController.getInstance().removeDangerousElement(stage1, event2);
        ErrorController.getInstance().removeDangerousElement(stage1, event3);
        ErrorController.getInstance().removeDangerousElement(stage2, event1);
        ErrorController.getInstance().removeDangerousElement(stage2, event2);
        ErrorController.getInstance().removeDangerousElement(stage2, event3);

        assertNull(ErrorController.getInstance().findDangerousElement(stage2, event1));
        assertNull(ErrorController.getInstance().findDangerousElement(stage2, event2));
        assertNull(ErrorController.getInstance().findDangerousElement(stage2, event3));
        assertNull(ErrorController.getInstance().findDangerousElement(stage1, event1));
        assertNull(ErrorController.getInstance().findDangerousElement(stage1, event2));
        assertNull(ErrorController.getInstance().findDangerousElement(stage1, event3));

        assertEquals("Wrong attempt count", 1, ErrorController.getInstance().registerDangerousElement(stage1, event1).getAttempts());
        assertEquals("Wrong attempt count", 1, ErrorController.getInstance().registerDangerousElement(stage1, event2).getAttempts());
        assertEquals("Wrong attempt count", 1, ErrorController.getInstance().registerDangerousElement(stage1, event3).getAttempts());
        assertEquals("Wrong attempt count", 1, ErrorController.getInstance().registerDangerousElement(stage2, event1).getAttempts());
        assertEquals("Wrong attempt count", 1, ErrorController.getInstance().registerDangerousElement(stage2, event2).getAttempts());
        assertEquals("Wrong attempt count", 1, ErrorController.getInstance().registerDangerousElement(stage2, event3).getAttempts());

        ErrorController.getInstance().removeDangerousElement(stage1, event1);
        ErrorController.getInstance().removeDangerousElement(stage1, event2);
        ErrorController.getInstance().removeDangerousElement(stage1, event3);
        ErrorController.getInstance().removeDangerousElement(stage2, event1);
        ErrorController.getInstance().removeDangerousElement(stage2, event2);
        ErrorController.getInstance().removeDangerousElement(stage2, event3);
    }

    public void testWithGC() {
        TestStage stage1 = new TestStage("stage1");
        TestStage stage2 = new TestStage("stage2");
        TestEvent event1 = new TestEvent("event1");
        TestEvent event2 = new TestEvent("event2");
        TestEvent event3 = new TestEvent("event3");

        assertEquals("Wrong attempt count", 1, ErrorController.getInstance().registerDangerousElement(stage1, event1).getAttempts());
        assertEquals("Wrong attempt count", 2, ErrorController.getInstance().registerDangerousElement(stage1, event1).getAttempts());
        assertEquals("Wrong attempt count", 3, ErrorController.getInstance().registerDangerousElement(stage1, event1).getAttempts());
        assertEquals("Wrong attempt count", 4, ErrorController.getInstance().registerDangerousElement(stage1, event1).getAttempts());

        assertEquals("Wrong attempt count", 1, ErrorController.getInstance().registerDangerousElement(stage1, event2).getAttempts());
        assertEquals("Wrong attempt count", 2, ErrorController.getInstance().registerDangerousElement(stage1, event2).getAttempts());
        assertEquals("Wrong attempt count", 3, ErrorController.getInstance().registerDangerousElement(stage1, event2).getAttempts());
        assertEquals("Wrong attempt count", 4, ErrorController.getInstance().registerDangerousElement(stage1, event2).getAttempts());
        assertEquals("Wrong attempt count", 5, ErrorController.getInstance().registerDangerousElement(stage1, event2).getAttempts());

        assertEquals("Wrong attempt count", 1, ErrorController.getInstance().registerDangerousElement(stage1, event3).getAttempts());
        assertEquals("Wrong attempt count", 2, ErrorController.getInstance().registerDangerousElement(stage1, event3).getAttempts());
        assertEquals("Wrong attempt count", 3, ErrorController.getInstance().registerDangerousElement(stage1, event3).getAttempts());
        assertEquals("Wrong attempt count", 4, ErrorController.getInstance().registerDangerousElement(stage1, event3).getAttempts());
        assertEquals("Wrong attempt count", 5, ErrorController.getInstance().registerDangerousElement(stage1, event3).getAttempts());
        assertEquals("Wrong attempt count", 6, ErrorController.getInstance().registerDangerousElement(stage1, event3).getAttempts());


        assertEquals("Wrong attempt count", 1, ErrorController.getInstance().registerDangerousElement(stage2, event1).getAttempts());
        assertEquals("Wrong attempt count", 2, ErrorController.getInstance().registerDangerousElement(stage2, event1).getAttempts());
        assertEquals("Wrong attempt count", 3, ErrorController.getInstance().registerDangerousElement(stage2, event1).getAttempts());
        assertEquals("Wrong attempt count", 4, ErrorController.getInstance().registerDangerousElement(stage2, event1).getAttempts());

        assertEquals("Wrong attempt count", 1, ErrorController.getInstance().registerDangerousElement(stage2, event2).getAttempts());
        assertEquals("Wrong attempt count", 2, ErrorController.getInstance().registerDangerousElement(stage2, event2).getAttempts());
        assertEquals("Wrong attempt count", 3, ErrorController.getInstance().registerDangerousElement(stage2, event2).getAttempts());
        assertEquals("Wrong attempt count", 4, ErrorController.getInstance().registerDangerousElement(stage2, event2).getAttempts());
        assertEquals("Wrong attempt count", 5, ErrorController.getInstance().registerDangerousElement(stage2, event2).getAttempts());

        assertEquals("Wrong attempt count", 1, ErrorController.getInstance().registerDangerousElement(stage2, event3).getAttempts());
        assertEquals("Wrong attempt count", 2, ErrorController.getInstance().registerDangerousElement(stage2, event3).getAttempts());
        assertEquals("Wrong attempt count", 3, ErrorController.getInstance().registerDangerousElement(stage2, event3).getAttempts());
        assertEquals("Wrong attempt count", 4, ErrorController.getInstance().registerDangerousElement(stage2, event3).getAttempts());
        assertEquals("Wrong attempt count", 5, ErrorController.getInstance().registerDangerousElement(stage2, event3).getAttempts());
        assertEquals("Wrong attempt count", 6, ErrorController.getInstance().registerDangerousElement(stage2, event3).getAttempts());

        event1 = null;
        event2 = null;
        event3 = null;

        System.gc();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        event1 = new TestEvent("event1");
        event2 = new TestEvent("event2");
        event3 = new TestEvent("event3");

        assertEquals("Wrong attempt count", 1, ErrorController.getInstance().registerDangerousElement(stage1, event1).getAttempts());
        assertEquals("Wrong attempt count", 2, ErrorController.getInstance().registerDangerousElement(stage1, event1).getAttempts());
        assertEquals("Wrong attempt count", 3, ErrorController.getInstance().registerDangerousElement(stage1, event1).getAttempts());
        assertEquals("Wrong attempt count", 4, ErrorController.getInstance().registerDangerousElement(stage1, event1).getAttempts());

        assertEquals("Wrong attempt count", 1, ErrorController.getInstance().registerDangerousElement(stage1, event2).getAttempts());
        assertEquals("Wrong attempt count", 2, ErrorController.getInstance().registerDangerousElement(stage1, event2).getAttempts());
        assertEquals("Wrong attempt count", 3, ErrorController.getInstance().registerDangerousElement(stage1, event2).getAttempts());
        assertEquals("Wrong attempt count", 4, ErrorController.getInstance().registerDangerousElement(stage1, event2).getAttempts());
        assertEquals("Wrong attempt count", 5, ErrorController.getInstance().registerDangerousElement(stage1, event2).getAttempts());

        assertEquals("Wrong attempt count", 1, ErrorController.getInstance().registerDangerousElement(stage1, event3).getAttempts());
        assertEquals("Wrong attempt count", 2, ErrorController.getInstance().registerDangerousElement(stage1, event3).getAttempts());
        assertEquals("Wrong attempt count", 3, ErrorController.getInstance().registerDangerousElement(stage1, event3).getAttempts());
        assertEquals("Wrong attempt count", 4, ErrorController.getInstance().registerDangerousElement(stage1, event3).getAttempts());
        assertEquals("Wrong attempt count", 5, ErrorController.getInstance().registerDangerousElement(stage1, event3).getAttempts());
        assertEquals("Wrong attempt count", 6, ErrorController.getInstance().registerDangerousElement(stage1, event3).getAttempts());


        assertEquals("Wrong attempt count", 1, ErrorController.getInstance().registerDangerousElement(stage2, event1).getAttempts());
        assertEquals("Wrong attempt count", 2, ErrorController.getInstance().registerDangerousElement(stage2, event1).getAttempts());
        assertEquals("Wrong attempt count", 3, ErrorController.getInstance().registerDangerousElement(stage2, event1).getAttempts());
        assertEquals("Wrong attempt count", 4, ErrorController.getInstance().registerDangerousElement(stage2, event1).getAttempts());

        assertEquals("Wrong attempt count", 1, ErrorController.getInstance().registerDangerousElement(stage2, event2).getAttempts());
        assertEquals("Wrong attempt count", 2, ErrorController.getInstance().registerDangerousElement(stage2, event2).getAttempts());
        assertEquals("Wrong attempt count", 3, ErrorController.getInstance().registerDangerousElement(stage2, event2).getAttempts());
        assertEquals("Wrong attempt count", 4, ErrorController.getInstance().registerDangerousElement(stage2, event2).getAttempts());
        assertEquals("Wrong attempt count", 5, ErrorController.getInstance().registerDangerousElement(stage2, event2).getAttempts());

        assertEquals("Wrong attempt count", 1, ErrorController.getInstance().registerDangerousElement(stage2, event3).getAttempts());
        assertEquals("Wrong attempt count", 2, ErrorController.getInstance().registerDangerousElement(stage2, event3).getAttempts());
        assertEquals("Wrong attempt count", 3, ErrorController.getInstance().registerDangerousElement(stage2, event3).getAttempts());
        assertEquals("Wrong attempt count", 4, ErrorController.getInstance().registerDangerousElement(stage2, event3).getAttempts());
        assertEquals("Wrong attempt count", 5, ErrorController.getInstance().registerDangerousElement(stage2, event3).getAttempts());
        assertEquals("Wrong attempt count", 6, ErrorController.getInstance().registerDangerousElement(stage2, event3).getAttempts());
    }
}
