package ru.kwanza.jeda.api.helper;

import ru.kwanza.jeda.api.*;
import ru.kwanza.jeda.api.ISystemManager;
import junit.framework.TestCase;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;

import static ru.kwanza.jeda.api.helper.SinkHelper.*;
import static org.mockito.Mockito.*;

public class TestSinkHelper extends TestCase {

    private static final String sinkName1 = "sinkName1";
    private static final String sinkName2 = "sinkName2";

    private SinkHelper sinkHelperSpy;
    private SinkResolver sinkResolverMock1;
    private SinkResolver sinkResolverMock2;

    private ISink<IEvent> sinkMock;

    private IEvent testEvent1 = new TestEvent("name1");
    private IEvent testEvent2 = new TestEvent("name2");
    private IEvent testEvent3 = new TestEvent("name3");
    private IEvent testEvent4 = new TestEvent("name4");

    private TestSink sink1;
    private TestSink sink2;

    private ISuspender<IEvent> suspenderMock;
    private ISystemManager systemManager;

    @Override
    public void setUp() throws Exception {
        sinkResolverMock1 = mock(SinkResolver.class);
        sinkResolverMock2 = mock(SinkResolver.class);

        //noinspection unchecked
        sinkMock = mock(ISink.class);

        testEvent1 = new TestEvent("name1");
        testEvent2 = new TestEvent("name2");
        testEvent3 = new TestEvent("name3");
        testEvent4 = new TestEvent("name4");

        sink1 = new TestSink(sinkName1);
        sink2 = new TestSink(sinkName2);

        //noinspection unchecked
        suspenderMock = mock(ISuspender.class);

        Mockito.doReturn(suspenderMock).when(sinkHelperSpy).getSuspender();
        when(sinkHelperSpy.getSink(sinkName1)).thenReturn(sink1);
        when(sinkHelperSpy.getSink(sinkName2)).thenReturn(sink2);

        systemManager = mock(ISystemManager.class);
        when(systemManager.resolveObjectName(anyString())).thenReturn("TestSink");
        when(systemManager.resolveObjectName(sink1)).thenReturn(sinkName1);
        when(systemManager.resolveObjectName(sink2)).thenReturn(sinkName2);
        when(systemManager.resolveObject(sinkName1)).thenReturn(new TestSink(sinkName1));
        when(systemManager.resolveObject(sinkName2)).thenReturn(new TestSink(sinkName2));

        sinkHelperSpy = spy(new SinkHelper(systemManager));
    }

    public void testFlushByNoSupportedObject() throws Exception {
        SinkHelper sinkHelper = new SinkHelper(systemManager);
        try {
            sinkHelper.flush(Arrays.asList(Color.ORANGE));
            TestCase.fail("Exception must be thrown.");
        } catch (RuntimeException e) {
            assertEquals("Object must be sink or sink name!", e.getMessage());
        }
    }

    public void testFlushBySinkNamePutBySinkName() throws Exception {
        when(sinkHelperSpy.getSink(anyString())).thenReturn(sinkMock);

        sinkHelperSpy.put("anyName", testEvent1);
        sinkHelperSpy.flush("sinkName");

        assertTestSinkPutIndeed(sinkMock, testEvent1);
    }

    public void testFlushBySinkNameTryPutBySinkName() throws Exception {
        when(sinkHelperSpy.getSink(anyString())).thenReturn(sinkResolverMock1);

        sinkHelperSpy.tryPut("anyName", testEvent1);
        sinkHelperSpy.flush("sinkName");

        assertTestSinkTryPutIndeed(sinkResolverMock1, testEvent1);
    }

    public void testFlushBySinkNameCollectionWithPutBySinkName() throws Exception {
        when(sinkHelperSpy.getSink(anyString())).thenReturn(sinkResolverMock1);

        sinkHelperSpy.put("anyName", testEvent1, testEvent1);
        sinkHelperSpy.flush(Arrays.asList("sinkName"));

        assertTestSinkPutIndeed(sinkResolverMock1, testEvent1);
    }

    public void testFlushBySinkNameCollectionWithTryPutBySinkName() throws Exception {
        when(sinkHelperSpy.getSink(anyString())).thenReturn(sinkResolverMock1);

        sinkHelperSpy.tryPut("anyName", testEvent1, testEvent1);
        sinkHelperSpy.flush(Arrays.asList("sinkName"));

        assertTestSinkTryPutIndeed(sinkResolverMock1, testEvent1);
    }

    public void testFlushNoParamsWithPutBySinkNameAndKey() throws Exception {
        when(sinkHelperSpy.getSink(anyString())).thenReturn(sinkResolverMock1);

        sinkHelperSpy.put("anyName", testEvent1);
        sinkHelperSpy.flush();

        assertTestSinkPutIndeed(sinkResolverMock1, testEvent1);
    }

    public void testFlushNoParamsWithTryPutBySinkNameAndKey() throws Exception {
        when(sinkHelperSpy.getSink(anyString())).thenReturn(sinkResolverMock1);

        sinkHelperSpy.tryPut("anyName", testEvent1);
        sinkHelperSpy.flush();

        assertTestSinkTryPutIndeed(sinkResolverMock1, testEvent1);
    }

    public void testFlushNoParamsWithPutBySinkName() throws Exception {
        when(sinkHelperSpy.getSink(anyString())).thenReturn(sinkResolverMock1);

        sinkHelperSpy.put("anyName", testEvent1);
        sinkHelperSpy.flush();

        assertTestSinkPutIndeed(sinkResolverMock1, testEvent1);
    }

    public void testFlushNoParamsWithTryPutBySinkName() throws Exception {
        when(sinkHelperSpy.getSink(anyString())).thenReturn(sinkResolverMock1);

        sinkHelperSpy.tryPut("anyName", testEvent1);
        sinkHelperSpy.flush();

        assertTestSinkTryPutIndeed(sinkResolverMock1, testEvent1);
    }

    public void testClear() throws Exception {
        when(sinkHelperSpy.getSink(anyString())).thenReturn(sinkResolverMock1);

        sinkHelperSpy.put("anyName", testEvent1);
        sinkHelperSpy.clear();
        sinkHelperSpy.flush();

        verify(sinkResolverMock1, never()).put(anyCollection());
    }


    public void testFlushNoParamsWithPutBySinkObj() throws Exception {
        SinkHelper sinkHelper = new SinkHelper(systemManager);

        sinkHelper.put(sinkResolverMock1, testEvent1);
        sinkHelper.flush();

        assertTestSinkPutIndeed(sinkResolverMock1, testEvent1);
    }

    public void testFlushNoParamsWithTryPutBySinkObj() throws Exception {
        SinkHelper sinkHelper = new SinkHelper(systemManager);

        sinkHelper.tryPut(sinkResolverMock1, testEvent1);
        sinkHelper.flush();

        assertTestSinkTryPutIndeed(sinkResolverMock1, testEvent1);
    }

    public void testFlushBySinkObjectsWithPutBySinkObj() throws Exception {
        SinkHelper sinkHelper = new SinkHelper(systemManager);

        sinkHelper.put(sinkResolverMock1, testEvent1);
        sinkHelper.flush(sinkResolverMock1);

        assertTestSinkPutIndeed(sinkResolverMock1, testEvent1);
    }

    public void testFlushBySinkObjectsWithTryPutBySinkObj() throws Exception {
        SinkHelper sinkHelper = new SinkHelper(systemManager);

        sinkHelper.tryPut(sinkResolverMock1, testEvent1);
        sinkHelper.flush(sinkResolverMock1);

        assertTestSinkTryPutIndeed(sinkResolverMock1, testEvent1);
    }

    public void testFlushBySinkObjectCollectionWithPutBySinkObj() throws Exception {
        SinkHelper sinkHelper = new SinkHelper(systemManager);

        sinkHelper.put(sinkResolverMock1, testEvent1);
        sinkHelper.flush(Arrays.asList(sinkResolverMock1));

        assertTestSinkPutIndeed(sinkResolverMock1, testEvent1);
    }

    public void testFlushBySinkObjectCollectionWithTryPutBySinkObj() throws Exception {
        SinkHelper sinkHelper = new SinkHelper(systemManager);

        sinkHelper.tryPut(sinkResolverMock1, testEvent1);
        sinkHelper.flush(Arrays.asList(sinkResolverMock1));

        assertTestSinkTryPutIndeed(sinkResolverMock1, testEvent1);
    }

    public void testFlushPutWithSinkException() throws Exception {
        String exceptionMessage = "Test Exception.";
        doThrow(new SinkException(exceptionMessage)).when(sinkResolverMock1).put(anyCollection());

        sinkHelperSpy.put(sinkResolverMock1, testEvent1);
        sinkHelperSpy.put(sinkResolverMock1, testEvent2);
        sinkHelperSpy.put(sinkResolverMock2, testEvent3);
        sinkHelperSpy.put(sinkResolverMock2, testEvent4);

        FlushResult flushResult = sinkHelperSpy.flush();
        assertEquals(1, flushResult.results.size());
        assertEquals(exceptionMessage, flushResult.get(sinkResolverMock1).getException().getMessage());

        List<TestEvent> declines = new ArrayList(flushResult.getDeclines());
        Collections.sort(declines);
        assertEquals(Arrays.asList(testEvent1, testEvent2), declines);
    }

    public void testFlushTryPutWithSinkException() throws Exception {
        when(sinkResolverMock2.tryPut(anyCollection())).thenReturn(null);

        String exceptionMessage = "Test Exception.";
        doThrow(new SinkException(exceptionMessage)).when(sinkResolverMock1).tryPut(anyCollection());

        sinkHelperSpy.tryPut(sinkResolverMock1, testEvent1);
        sinkHelperSpy.tryPut(sinkResolverMock1, testEvent2);
        sinkHelperSpy.tryPut(sinkResolverMock2, testEvent3);
        sinkHelperSpy.tryPut(sinkResolverMock2, testEvent4);

        FlushResult flushResult = sinkHelperSpy.flush();
        assertEquals(1, flushResult.results.size());
        assertEquals(exceptionMessage, flushResult.get(sinkResolverMock1).getException().getMessage());
        List<TestEvent> declines = new ArrayList(flushResult.getDeclines());
        Collections.sort(declines);
        assertEquals(Arrays.asList(testEvent1, testEvent2), declines);
    }

    private void prepareFlushOrSuspendTest() throws Exception {
        List<IEvent> sink1Events = Arrays.asList(testEvent1, testEvent2);
        List<IEvent> sink2Events = Arrays.asList(testEvent3, testEvent4);

        when(suspenderMock.suspend(any(ISink.class), anyCollection())).thenReturn(sink1Events, sink2Events);

        doThrow(new SinkException.Clogged("")).when(sinkResolverMock1).put(anyCollection());
        doThrow(new SinkException.Clogged("")).when(sinkResolverMock2).put(anyCollection());

        when(sinkHelperSpy.getSink(anyString())).thenReturn(sinkResolverMock1, sinkResolverMock2);

        sinkHelperSpy.put(sinkResolverMock1, testEvent1);
        sinkHelperSpy.put(sinkResolverMock1, testEvent2);
        sinkHelperSpy.put(sinkResolverMock2, testEvent3);
        sinkHelperSpy.put(sinkResolverMock2, testEvent4);
    }

    public void testFlushOrSuspendBySinkArray() throws Exception {
        prepareFlushOrSuspendTest();

        List<TestEvent> suspendedEvents =  new ArrayList(sinkHelperSpy.flushOrSuspend(sinkResolverMock1, sinkResolverMock2));
        Collections.sort(suspendedEvents);

        assertEquals(Arrays.asList(testEvent1, testEvent2, testEvent3, testEvent4), suspendedEvents);
    }

    public void testFlushOrSuspendBySinkCollection() throws Exception {
        prepareFlushOrSuspendTest();

        List<TestEvent> suspendedEvents =  new ArrayList(sinkHelperSpy.flushOrSuspend(Arrays.asList(sinkResolverMock1, sinkResolverMock2)));
        Collections.sort(suspendedEvents);

        assertEquals(Arrays.asList(testEvent1, testEvent2, testEvent3, testEvent4), suspendedEvents);
    }

    public void testFlushOrSuspendNoParams() throws Exception {
        prepareFlushOrSuspendTest();

        List<TestEvent> suspendedEvents =  new ArrayList(sinkHelperSpy.flushOrSuspend());
        Collections.sort(suspendedEvents);

        assertEquals(Arrays.asList(testEvent1, testEvent2, testEvent3, testEvent4), suspendedEvents);
    }

    public void testFlushOrSuspendBySinkNames() throws Exception {
        prepareFlushOrSuspendTest();

        List<TestEvent> suspendedEvents =  new ArrayList(sinkHelperSpy.flushOrSuspend("sinkRes1", "sinkRes2"));
        Collections.sort(suspendedEvents);

        assertEquals(Arrays.asList(testEvent1, testEvent2, testEvent3, testEvent4), suspendedEvents);
    }


    public void testRefuseBySinkNames() throws Exception {
        sinkHelperSpy.tryPut(sink1, getTestEvents().get(0));
        sinkHelperSpy.tryPut(sink2, getTestEvents().get(1));
        sinkHelperSpy.put(sink2, getTestEvents().get(2));

        @SuppressWarnings("unchecked") List<TestEvent> refusedEvents = new ArrayList(sinkHelperSpy.refuse(sinkName1, sinkName2));
        Collections.sort(refusedEvents);
        assertEquals(3, refusedEvents.size());

        assertRefusedEvent(refusedEvents.get(0), sinkName1);
        assertRefusedEvent(refusedEvents.get(1), sinkName2);
        assertRefusedEvent(refusedEvents.get(2), sinkName2);
    }

    public void testRefuseBySinkArray() throws Exception {
        SinkHelper sinkHelper = new SinkHelper(systemManager);

        sinkHelper.put(sink1, getTestEvents().get(0));
        sinkHelper.put(sink2, getTestEvents().get(1));
        sinkHelper.put(sink2, getTestEvents().get(2));

        @SuppressWarnings("unchecked") List<TestEvent> refusedEvents = new ArrayList(sinkHelper.refuse(sink1, sink2));
        Collections.sort(refusedEvents);
        assertEquals(3, refusedEvents.size());

        assertRefusedEvent(refusedEvents.get(0), sinkName1);
        assertRefusedEvent(refusedEvents.get(1), sinkName2);
        assertRefusedEvent(refusedEvents.get(2), sinkName2);
    }

    public void testRefuseByKeyCollection() throws Exception {
        SinkHelper sinkHelper = new SinkHelper(systemManager);

        List<IEvent> testEvents = getTestEvents();

        sinkHelper.tryPut(sink1, testEvents.get(0));
        sinkHelper.put(sink2, testEvents.get(1));
        sinkHelper.put(sink2, testEvents.get(2));

        @SuppressWarnings("UnnecessaryLocalVariable") Collection keyCollection = testEvents;
        @SuppressWarnings("unchecked") List<TestEvent> refusedEvents = new ArrayList(sinkHelper.refuse(keyCollection));
        Collections.sort(refusedEvents);
        assertEquals(3, refusedEvents.size());

        assertRefusedEvent(refusedEvents.get(0), sinkName1);
        assertRefusedEvent(refusedEvents.get(1), sinkName2);
        assertRefusedEvent(refusedEvents.get(2), sinkName2);
    }

    public void testRefuseByKeyCollectionAndSinkNameArray() throws Exception {
        List<IEvent> testEvents = getTestEvents();

        sinkHelperSpy.put(sink1, testEvents.get(0));
        sinkHelperSpy.put(sink2, testEvents.get(1));
        sinkHelperSpy.put(sink2, testEvents.get(2));

        @SuppressWarnings("unchecked") List<TestEvent> refusedEvents = new ArrayList(sinkHelperSpy.refuse(testEvents, sinkName1, sinkName2));
        Collections.sort(refusedEvents);
        assertEquals(3, refusedEvents.size());

        assertRefusedEvent(refusedEvents.get(0), sinkName1);
        assertRefusedEvent(refusedEvents.get(1), sinkName2);
        assertRefusedEvent(refusedEvents.get(2), sinkName2);
    }

    public void testRefuseByKeyCollectionAndSinkArray() throws Exception {
        List<IEvent> testEvents = getTestEvents();

        sinkHelperSpy.put(sink1, testEvents.get(0));
        sinkHelperSpy.put(sink2, testEvents.get(1));
        sinkHelperSpy.put(sink2, testEvents.get(2));

        @SuppressWarnings("unchecked") List<TestEvent> refusedEvents = new ArrayList(sinkHelperSpy.refuse(testEvents, sink1, sink2));
        Collections.sort(refusedEvents);
        assertEquals(3, refusedEvents.size());

        assertRefusedEvent(refusedEvents.get(0), sinkName1);
        assertRefusedEvent(refusedEvents.get(1), sinkName2);
        assertRefusedEvent(refusedEvents.get(2), sinkName2);
    }

    public void testRefuseByKeyCollectionAndSinkCollection() throws Exception {
        Collection sinks = Arrays.asList(sink1, sink2);
        commonTestRefuseByKeyCollectionAndCollection(sinks);
    }

    public void testRefuseByKeyCollectionAndSinkNamesCollection() throws Exception {
        Collection sinks = Arrays.asList(sinkName1, sinkName2);
        commonTestRefuseByKeyCollectionAndCollection(sinks);
    }

    public void testRefuseByKeyCollectionAndStrangeObjects() throws Exception {
        Collection sinks = Arrays.asList(Color.CYAN, Color.OPAQUE);
        try {
            commonTestRefuseByKeyCollectionAndCollection(sinks);
            fail("Exception must be thrown.");
        } catch (Exception e) {
            assertEquals("Object must be sink or sink name!", e.getMessage());
        }
    }

    public void testRefuseAndSuspendBySinkNameArray() throws Exception {

        List<IEvent> sink1Events = Arrays.asList(testEvent1, testEvent2);
        List<IEvent> sink2Events = Arrays.asList(testEvent3, testEvent4);

        final List<IEvent> eventsToSuspend = new ArrayList<IEvent>(sink1Events);
        eventsToSuspend.addAll(sink2Events);

        sinkHelperSpy.put(sinkName1, testEvent1);
        sinkHelperSpy.put(sinkName1, testEvent2);
        sinkHelperSpy.put(sinkName2, testEvent3);
        sinkHelperSpy.put(sinkName2, testEvent4);

        List<TestEvent> refusedAndSuspendedEvents = new ArrayList(sinkHelperSpy.refuseAndSuspend(sinkName1, sinkName2));
        Collections.sort(refusedAndSuspendedEvents);
        assertEquals(eventsToSuspend, refusedAndSuspendedEvents);

        verify(suspenderMock).suspend(sinkName1, sink1Events);
        verify(suspenderMock).suspend(sinkName2, sink2Events);
        verify(suspenderMock).flush();
    }

    public void testRefuseAndSuspendBySinkArray() throws Exception {
        List<IEvent> sink1Events = Arrays.asList(testEvent1, testEvent2);
        List<IEvent> sink2Events = Arrays.asList(testEvent3, testEvent4);

        final List<IEvent> eventsToSuspend = new ArrayList<IEvent>(sink1Events);
        eventsToSuspend.addAll(sink2Events);

        sinkHelperSpy.put(sink1, testEvent1);
        sinkHelperSpy.put(sink1, testEvent2);
        sinkHelperSpy.put(sink2, testEvent3);
        sinkHelperSpy.put(sink2, testEvent4);

        List<TestEvent> refusedAndSuspendedEvents = new ArrayList(sinkHelperSpy.refuseAndSuspend(sink1, sink2));
        Collections.sort(refusedAndSuspendedEvents);
        assertEquals(eventsToSuspend, refusedAndSuspendedEvents);

        verify(suspenderMock).suspend(sinkName1, sink1Events);
        verify(suspenderMock).suspend(sinkName2, sink2Events);
        verify(suspenderMock).flush();
    }

    public void testRefuseAndSuspendByKeyCollection() throws Exception {
        List<IEvent> sink1Events = Arrays.asList(testEvent1, testEvent2);
        List<IEvent> sink2Events = Arrays.asList(testEvent3, testEvent4);

        final List<IEvent> eventsToSuspend = new ArrayList<IEvent>(sink1Events);
        eventsToSuspend.addAll(sink2Events);

        sinkHelperSpy.put(sink1, testEvent1);
        sinkHelperSpy.put(sink1, testEvent2);
        sinkHelperSpy.put(sink2, testEvent3);
        sinkHelperSpy.put(sink2, testEvent4);

        List<TestEvent> refusedAndSuspendedEvents = new ArrayList(sinkHelperSpy.refuseAndSuspend(eventsToSuspend));
        Collections.sort(refusedAndSuspendedEvents);
        assertEquals(eventsToSuspend, refusedAndSuspendedEvents);

        verify(suspenderMock).suspend(sinkName1, sink1Events);
        verify(suspenderMock).suspend(sinkName2, sink2Events);
        verify(suspenderMock).flush();
    }

    public void testRefuseAndSuspendByKeyCollectionAndSinkNameArray() throws Exception {
        List<IEvent> sink1Events = Arrays.asList(testEvent1, testEvent2);
        List<IEvent> sink2Events = Arrays.asList(testEvent3, testEvent4);

        final List<IEvent> eventsToSuspend = new ArrayList<IEvent>(sink1Events);
        eventsToSuspend.addAll(sink2Events);

        sinkHelperSpy.put(sink1, testEvent1);
        sinkHelperSpy.put(sink1, testEvent2);
        sinkHelperSpy.put(sink2, testEvent3);
        sinkHelperSpy.put(sink2, testEvent4);

        List<TestEvent> refusedAndSuspendedEvents = new ArrayList(sinkHelperSpy.refuseAndSuspend(eventsToSuspend, sinkName1, sinkName2));
        Collections.sort(refusedAndSuspendedEvents);
        assertEquals(eventsToSuspend, refusedAndSuspendedEvents);

        verify(suspenderMock).suspend(sinkName1, sink1Events);
        verify(suspenderMock).suspend(sinkName2, sink2Events);
        verify(suspenderMock).flush();
    }

    public void testRefuseAndSuspendByKeyCollectionAndSinkArray() throws Exception {
        List<IEvent> sink1Events = Arrays.asList(testEvent1, testEvent2);
        List<IEvent> sink2Events = Arrays.asList(testEvent3, testEvent4);

        final List<IEvent> eventsToSuspend = new ArrayList<IEvent>(sink1Events);
        eventsToSuspend.addAll(sink2Events);

        sinkHelperSpy.put(sink1, testEvent1);
        sinkHelperSpy.put(sink1, testEvent2);
        sinkHelperSpy.put(sink2, testEvent3);
        sinkHelperSpy.put(sink2, testEvent4);

        List<TestEvent> refusedAndSuspendedEvents = new ArrayList(sinkHelperSpy.refuseAndSuspend(eventsToSuspend, sink1, sink2));
        Collections.sort(refusedAndSuspendedEvents);
        assertEquals(eventsToSuspend, refusedAndSuspendedEvents);

        verify(suspenderMock).suspend(sinkName1, sink1Events);
        verify(suspenderMock).suspend(sinkName2, sink2Events);
        verify(suspenderMock).flush();
    }

    public void testRefuseAndSuspendByKeyCollectionAndSinkCollection() throws Exception {
        List<IEvent> sink1Events = Arrays.asList(testEvent1, testEvent2);
        List<IEvent> sink2Events = Arrays.asList(testEvent3, testEvent4);

        final List<IEvent> eventsToSuspend = new ArrayList<IEvent>(sink1Events);
        eventsToSuspend.addAll(sink2Events);

        sinkHelperSpy.put(sink1, testEvent1);
        sinkHelperSpy.put(sink1, testEvent2);
        sinkHelperSpy.put(sink2, testEvent3);
        sinkHelperSpy.put(sink2, testEvent4);

        List<TestEvent> refusedAndSuspendedEvents = new ArrayList(sinkHelperSpy.refuseAndSuspend(eventsToSuspend, Arrays.asList(sink1, sink2)));
        Collections.sort(refusedAndSuspendedEvents);
        assertEquals(eventsToSuspend, refusedAndSuspendedEvents);

        verify(suspenderMock).suspend(sinkName1, sink1Events);
        verify(suspenderMock).suspend(sinkName2, sink2Events);
        verify(suspenderMock).flush();
    }

    public void testRefuseAndSuspendByKeyCollectionAndSinkNameCollection() throws Exception {
        List<IEvent> sink1Events = Arrays.asList(testEvent1, testEvent2);
        List<IEvent> sink2Events = Arrays.asList(testEvent3, testEvent4);

        final List<IEvent> eventsToSuspend = new ArrayList<IEvent>(sink1Events);
        eventsToSuspend.addAll(sink2Events);

        sinkHelperSpy.put(sink1, testEvent1);
        sinkHelperSpy.put(sink1, testEvent2);
        sinkHelperSpy.put(sink2, testEvent3);
        sinkHelperSpy.put(sink2, testEvent4);

        List<TestEvent> refusedAndSuspendedEvents = new ArrayList(sinkHelperSpy.refuseAndSuspend(eventsToSuspend, Arrays.asList(sinkName1, sinkName2)));
        Collections.sort(refusedAndSuspendedEvents);
        assertEquals(eventsToSuspend, refusedAndSuspendedEvents);

        verify(suspenderMock).suspend(sinkName1, sink1Events);
        verify(suspenderMock).suspend(sinkName2, sink2Events);
        verify(suspenderMock).flush();
    }

    public void testGetSuspender() throws Exception {
        ISystemManager systemManagerMock = mock(ISystemManager.class);

        IPendingStore pendingStoreMock = mock(IPendingStore.class);
        when(systemManagerMock.getPendingStore()).thenReturn(pendingStoreMock);
        ISuspender<IEvent> suspenderMock = mock(ISuspender.class);
        when(pendingStoreMock.getSuspender()).thenReturn(suspenderMock);


        SinkHelper sinkHelper = new SinkHelper(systemManagerMock);
        assertTrue(suspenderMock == sinkHelper.getSuspender());
    }

    private void commonTestRefuseByKeyCollectionAndCollection(Collection sinksOrSinkNamesCollection) throws Exception {
        List<IEvent> testEvents = getTestEvents();

        sinkHelperSpy.put(sink1, testEvents.get(0));
        sinkHelperSpy.put(sink2, testEvents.get(1));
        sinkHelperSpy.put(sink2, testEvents.get(2));

        @SuppressWarnings("unchecked") List<TestEvent> refusedEvents =
                new ArrayList(sinkHelperSpy.refuse(testEvents, sinksOrSinkNamesCollection));
        Collections.sort(refusedEvents);
        assertEquals(3, refusedEvents.size());

        assertRefusedEvent(refusedEvents.get(0), sinkName1);
        assertRefusedEvent(refusedEvents.get(1), sinkName2);
        assertRefusedEvent(refusedEvents.get(2), sinkName2);
    }

    private void assertRefusedEvent(IEvent event, String sinkName) {
        assertEquals(sinkName, SinkHelper.SH_SINK_NAME_ATTR.get(event));
        assertEquals(event, SinkHelper.SH_EVENT_KEY_ATTR.get(event));
    }

    private void assertTestSinkPutIndeed(ISink sinkMock, IEvent event) throws SinkException {
        ArgumentCaptor<Collection> argument = ArgumentCaptor.forClass(Collection.class);
        //noinspection unchecked
        verify(sinkMock).put(argument.capture());

        assertEquals(1, argument.getAllValues().size());
        assertTrue(argument.getValue().contains(event));
    }

    private void assertTestSinkTryPutIndeed(ISink sinkMock, IEvent event) throws SinkException {
        ArgumentCaptor<Collection> argument = ArgumentCaptor.forClass(Collection.class);
        //noinspection unchecked
        verify(sinkMock).tryPut(argument.capture());

        assertEquals(1, argument.getAllValues().size());
        assertTrue(argument.getValue().contains(event));
    }

    private List<IEvent> getTestEvents() {
        return new ArrayList<IEvent>(Arrays.asList(new TestEvent("1"), new TestEvent("2"), new TestEvent("3")));
    }

    private static class TestEvent extends AbstractEvent implements Comparable<TestEvent> {

        private String name;

        private TestEvent(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestEvent event = (TestEvent) o;

            return !(name != null ? !name.equals(event.name) : event.name != null);

        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }


        public int compareTo(TestEvent o) {
            return this.name.compareTo(o.name);
        }

    }

    private static class TestSink implements ISink<IEvent> {

        private String id;

        private TestSink(String id) {
            this.id = id;
        }

        public void put(Collection<IEvent> events) throws SinkException {
            System.out.println("Call TestSink.put(events)");
        }

        public Collection<IEvent> tryPut(Collection<IEvent> events) throws SinkException {
            System.out.println("Call TestSink.tryPut(events)");
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestSink testSink = (TestSink) o;

            return !(id != null ? !id.equals(testSink.id) : testSink.id != null);

        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }

    }

}