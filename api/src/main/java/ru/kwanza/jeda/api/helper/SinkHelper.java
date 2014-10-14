package ru.kwanza.jeda.api.helper;

import ru.kwanza.jeda.api.*;
import ru.kwanza.toolbox.attribute.AttributeFactory;
import ru.kwanza.toolbox.attribute.AttributeField;
import ru.kwanza.toolbox.attribute.IAttribute;
import ru.kwanza.toolbox.fieldhelper.FieldHelper;
import ru.kwanza.toolbox.splitter.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Guzanov Alexander
 */
public class SinkHelper {

    private static final Logger logger = LoggerFactory.getLogger(SinkHelper.class);

    public static final IAttribute<Object> SH_EVENT_KEY_ATTR = AttributeFactory.create();
    public static final IAttribute<String> SH_SINK_NAME_ATTR = AttributeFactory.create();
    public static final FieldHelper.Field<IEvent, String> SH_SINK_NAME_FIELD = new AttributeField<IEvent, String>(SH_SINK_NAME_ATTR);

    private final ISystemManager manager;
    private Map<ISink, Map<Object, IEvent>> puts = new HashMap<ISink, Map<Object, IEvent>>();
    private Map<ISink, Map<Object, IEvent>> tryPuts = new HashMap<ISink, Map<Object, IEvent>>();
    private Map<String, ISink> sinks = new HashMap<String, ISink>();

    public SinkHelper(ISystemManager manager){
        this.manager = manager;
    }

    public void clear() {
        puts.clear();
        tryPuts.clear();
        sinks.clear();
    }

    ISink getSink(String name) {
        ISink result = sinks.get(name);
        if (result == null) {
            result = new SinkResolver(manager,name);
            sinks.put(name, result);
        }
        return result;
    }

    public void put(String name, Object key, IEvent event) {
        put(getSink(name), key, event);
    }

    public void put(String name, IEvent event) {
        put(getSink(name), event, event);
    }

    public void put(ISink sink, Object key, IEvent event) {
        put(sink, key, puts, event);
    }

    public void put(ISink sink, IEvent event) {
        put(sink, event, puts, event);
    }

//    public static Map<ISink, Map<Object, IEvent>> splitByKeyBySink(Collection<IEvent> events) {
//        return new Splitter<IEvent>(SH_SINK_FIELD, SH_EVENT_KEY_FIELD).oneToOne(events);
//    }
//
//    public static Map<String, Map<Object, IEvent>> splitByKeyBySinkName(Collection<IEvent> events) {
//        return new Splitter<IEvent>(SH_SINK_NAME_FIELD, SH_EVENT_KEY_FIELD).oneToOne(events);
//    }

    /**
     * @param sinkName синки
     * @return коллекция ивентов с установленными атрибутами SH_EVENT_KEY_ATTR и SH_SINK_NAME_ATTR
     */
    public Collection<IEvent> refuse(String... sinkName) {
        List<IEvent> eventList = new LinkedList<IEvent>();
        for (String s : sinkName) {
            ISink sink = getSink(s);
            setEventsAttributes(s, puts.remove(sink), eventList);
            setEventsAttributes(s, tryPuts.remove(sink), eventList);
        }
        return eventList;
    }

    /**
     * @param sinks синки
     * @return коллекция ивентов с установленными атрибутами SH_EVENT_KEY_ATTR и SH_SINK_NAME_ATTR
     */
    public Collection<IEvent> refuse(ISink... sinks) {
        List<IEvent> eventList = new LinkedList<IEvent>();
        for (ISink s : sinks) {
            String sinkName = manager.resolveObjectName(s);
            setEventsAttributes(sinkName, puts.remove(s), eventList);
            setEventsAttributes(sinkName, tryPuts.remove(s), eventList);
        }
        return eventList;
    }

    /**
     * @param keys ключи событий
     * @return коллекция ивентов с установленными атрибутами SH_EVENT_KEY_ATTR и SH_SINK_NAME_ATTR
     */
    public Collection<IEvent> refuse(Collection keys) {
        List<IEvent> eventList = new LinkedList<IEvent>();
        HashSet<ISink> sinks = new HashSet<ISink>(this.puts.keySet());
        sinks.addAll(this.tryPuts.keySet());
        for (ISink sink : sinks) {
            refuseKeysInSink(keys, sink, eventList);
        }
        return eventList;
    }

    /**
     * @param keys     ключи событий
     * @param sinkName имя синка
     * @return коллекция ивентов с установленными атрибутами SH_EVENT_KEY_ATTR и SH_SINK_NAME_ATTR
     */
    public Collection<IEvent> refuse(Collection keys, String... sinkName) {
        List<IEvent> eventList = new LinkedList<IEvent>();
        for (String s : sinkName) {
            ISink sink = getSink(s);
            refuseKeysInSink(keys, sink, eventList);
        }
        return eventList;
    }

    /**
     * @param keys  ключи событий
     * @param sinks синки
     * @return коллекция ивентов с установленными атрибутами SH_EVENT_KEY_ATTR и SH_SINK_NAME_ATTR
     */
    public Collection<IEvent> refuse(Collection keys, ISink... sinks) {
        List<IEvent> eventList = new LinkedList<IEvent>();
        for (ISink sink : sinks) {
            refuseKeysInSink(keys, sink, eventList);
        }
        return eventList;
    }

    /**
     * @param keys  ключи событий
     * @param sinks синки
     * @return коллекция ивентов с установленными атрибутами SH_EVENT_KEY_ATTR и SH_SINK_NAME_ATTR
     */
    public Collection<IEvent> refuse(Collection keys, Collection sinks) {
        List<IEvent> eventList = new LinkedList<IEvent>();
        for (Object obj : sinks) {
            ISink sink;
            if (obj instanceof String) {
                sink = getSink((String) obj);
            } else if (obj instanceof ISink) {
                sink = (ISink) obj;
            } else {
                throw new RuntimeException("Object must be sink or sink name!");
            }

            refuseKeysInSink(keys, sink, eventList);
        }
        return eventList;
    }

    //Map<ISink, Map<Object, Long>>

    /**
     * @param sinkNames синки
     * @return коллекция событий с атрибутами установленными следующими методами: {@linkplain #refuse} и {@linkplain #suspendEvents}
     * @see #refuse
     * @see #suspendEvents
     */
    public Collection<IEvent> refuseAndSuspend(String... sinkNames) {
        return suspendEvents(refuse(sinkNames));
    }

    /**
     * @param sinks синки
     * @return коллекция событий с атрибутами установленными следующими методами: {@linkplain #refuse} и {@linkplain #suspendEvents}
     * @see #refuse
     * @see #suspendEvents
     */
    public Collection<IEvent> refuseAndSuspend(ISink... sinks) {
        return suspendEvents(refuse(sinks));
    }

    /**
     * @param keys ключи событий
     * @return коллекция событий с атрибутами установленными следующими методами: {@linkplain #refuse} и {@linkplain #suspendEvents}
     * @see #refuse
     * @see #suspendEvents
     */
    public Collection<IEvent> refuseAndSuspend(Collection keys) {
        return suspendEvents(refuse(keys));
    }

    /**
     * @param keys     ключи событий
     * @param sinkName синк
     * @return коллекция событий с атрибутами установленными следующими методами: {@linkplain #refuse} и {@linkplain #suspendEvents}
     * @see #refuse
     * @see #suspendEvents
     */
    public Collection<IEvent> refuseAndSuspend(Collection keys, String... sinkName) {
        return suspendEvents(refuse(keys, sinkName));
    }

    /**
     * @param keys  ключи событий
     * @param sinks синки
     * @return коллекция событий с атрибутами установленными следующими методами: {@linkplain #refuse} и {@linkplain #suspendEvents}
     * @see #refuse
     * @see #suspendEvents
     */
    public Collection<IEvent> refuseAndSuspend(Collection keys, ISink... sinks) {
        return suspendEvents(refuse(keys, sinks));
    }

    /**
     * @param keys  ключи событий
     * @param sinks синки
     * @return коллекция событий с атрибутами установленными следующими методами: {@linkplain #refuse} и {@linkplain #suspendEvents}
     * @see #refuse
     * @see #suspendEvents
     */
    public Collection<IEvent> refuseAndSuspend(Collection keys, Collection sinks) {
        return suspendEvents(refuse(keys, sinks));
    }

    public FlushResult flush(ISink... sinks) {
        FlushResult result = new FlushResult();
        for (ISink sink : sinks) {
            flushSink(sink, result);
        }

        return result;
    }

    public FlushResult flush(Collection sinks) {
        FlushResult result = new FlushResult();
        for (Object sink : sinks) {
            if (sink instanceof String) {
                flushSink(getSink((String) sink), result);
            } else if (sink instanceof ISink) {
                flushSink((ISink) sink, result);
            } else {
                throw new RuntimeException("Object must be sink or sink name!");
            }
        }

        return result;
    }

    public FlushResult flush() {
        FlushResult result = new FlushResult();
        HashSet<ISink> allSinks = new HashSet<ISink>(tryPuts.keySet());
        allSinks.addAll(puts.keySet());
        for (ISink sink : allSinks) {
            flushSink(sink, result);
        }

        return result;
    }

    public FlushResult flush(String... sinks) {
        FlushResult result = new FlushResult();
        for (String sinkName : sinks) {
            flushSink(getSink(sinkName), result);
        }

        return result;
    }

    /**
     * Метод пытается зафлашить ивенты в синк, а в случае провала саспендит их
     *
     * @param sinks синки
     * @return коллекция событий, которые были засаспенжены с проставлением соответствующих атрибутов
     * @throws SuspendException
     */
    public Collection<IEvent> flushOrSuspend(ISink... sinks) throws SuspendException {
        return flushOrSuspend(flush(sinks));
    }

    /**
     * Метод пытается зафлашить ивенты в синк, а в случае провала саспендит их
     *
     * @param sinks синки
     * @return коллекция событий, которые были засаспенжены с проставлением соответствующих атрибутов
     * @throws SuspendException
     */
    public Collection<IEvent> flushOrSuspend(Collection sinks) throws SuspendException {
        return flushOrSuspend(flush(sinks));
    }

    /**
     * Метод пытается зафлашить ивенты в синк, а в случае провала саспендит их
     *
     * @return коллекция событий, которые были засаспенжены с проставлением соответствующих атрибутов
     * @throws SuspendException
     */
    public Collection<IEvent> flushOrSuspend() throws SuspendException {
        return flushOrSuspend(flush());
    }

    /**
     * Метод пытается зафлашить ивенты в синк, а в случае провала саспендит их
     *
     * @param sinks синки
     * @return коллекция событий, которые были засаспенжены с проставлением соответствующих атрибутов
     * @throws SuspendException
     */
    public Collection<IEvent> flushOrSuspend(String... sinks) throws SuspendException {
        return flushOrSuspend(flush(sinks));
    }

    public void flushSink(ISink sink, FlushResult result) {
        flushPutsToSink(sink, result);
        flushTryPutsToSink(sink, result);
    }

    private Collection<IEvent> flushOrSuspend(FlushResult result) throws SuspendException {
        List<IEvent> events = new LinkedList<IEvent>();
        ISuspender<IEvent> suspender = getSuspender();
        for (Map.Entry<ISink, Collection<IEvent>> entry : result.getDeclinesMapBySink().entrySet()) {
            events.addAll(suspender.suspend(entry.getKey(), entry.getValue()));
        }
        suspender.flush();
        return events;
    }

    protected ISuspender<IEvent> getSuspender() {
        return manager.getPendingStore().getSuspender();
    }

    private void setEventsAttributes(String sinkName, Map<Object, IEvent> eventByKey, Collection<IEvent> outputEvents) {
        if (eventByKey == null) {
            return;
        }

        for (Map.Entry<Object, IEvent> entry : eventByKey.entrySet()) {
            IEvent event = entry.getValue();
            setEventAttributes(event, sinkName, entry.getKey());
            outputEvents.add(event);
        }
    }

    private void setEventAttributes(IEvent event, String sinkName, Object key) {
        SH_SINK_NAME_ATTR.set(event, sinkName);
        SH_EVENT_KEY_ATTR.set(event, key);
    }

    /**
     * @param events коллекция событий, которые нужно засаспендить
     * @return полученная на входе коллекция с установленными атрибутами IPendingStore#SUSPEND_ID_ATTR и IPendingStore#SUSPEND_SINK_NAME_ATTR
     */
    private Collection<IEvent> suspendEvents(Collection<IEvent> events) {
        ISuspender<IEvent> suspender = getSuspender();

        Map<String, Collection<IEvent>> eventsBySinkName =
                new Splitter<IEvent>(SH_SINK_NAME_FIELD).oneToMany(events);

        for (Map.Entry<String, Collection<IEvent>> entry : eventsBySinkName.entrySet()) {
            suspender.suspend(entry.getKey(), entry.getValue());
        }

        suspender.flush();
        return events;
    }

    private void flushPutsToSink(ISink sink, FlushResult result) {
        Map<Object, IEvent> events = puts.remove(sink);
        if (events == null || events.isEmpty()) {
            return;
        }
        Collection<IEvent> values = events.values();
        boolean traceEnabled = logger.isTraceEnabled();
        if (traceEnabled) {
            logger.trace("Flushing 'puts' to Sink {}, count={}", getSinkName(sink), values.size());
        }
        try {
            sink.put(values);
        } catch (SinkException ex) {
            if (traceEnabled) {
                logger.trace("Flushing error to sink {}, count={}", getSinkName(sink), values.size());
                logger.trace("Flushing error", ex);
            }
            result.results.put(sink, new FlushResult.Entry(sink, values, ex));
        }
    }

    private String getSinkName(ISink sink) {
        if (sink instanceof SinkResolver) {
            return ((SinkResolver) sink).getName();
        }
        return manager.resolveObjectName(sink);
    }

    private void flushTryPutsToSink(ISink sink, FlushResult result) {
        Map<Object, IEvent> events = tryPuts.remove(sink);
        if (events == null || events.isEmpty()) {
            return;
        }
        Collection<IEvent> values = events.values();
        boolean traceEnabled = logger.isTraceEnabled();
        if (traceEnabled) {
            logger.trace("Flushing 'tryPuts' to Sink {}, count={}", getSinkName(sink), values.size());
        }
        try {
            Collection collection = sink.tryPut(values);
            if (collection != null) {
                if (traceEnabled) {
                    logger.trace("Flushing declines to sink {}, decline count={}", getSinkName(sink),
                            collection.size());
                }
                result.results.put(sink, new FlushResult.Entry(sink, collection));
            }
        } catch (SinkException ex) {
            if (traceEnabled) {
                logger.trace("Flushing error to sink {}, count={}", getSinkName(sink), values.size());
                logger.trace("Flushing error", ex);
            }
            result.results.put(sink, new FlushResult.Entry(sink, values, ex));
        }
    }

    public void tryPut(String name, Object key, IEvent event) {
        tryPut(getSink(name), key, event);
    }

    public void tryPut(String name, IEvent event) {
        tryPut(getSink(name), event, event);
    }

    public void tryPut(ISink sink, Object key, IEvent event) {
        put(sink, key, tryPuts, event);
    }

    public void tryPut(ISink sink, IEvent event) {
        put(sink, event, tryPuts, event);
    }

    private void put(ISink sink, Object key, Map<ISink, Map<Object, IEvent>> map, IEvent event) {
        Map<Object, IEvent> events = map.get(sink);
        if (events == null) {
            events = new HashMap<Object, IEvent>();
            map.put(sink, events);
        }
        events.put(key, event);
    }

    private void refuseKeys(Collection<Object> keys,
                            Map<Object, IEvent> puts,
                            Map<Object, IEvent> tryPuts,
                            String sinkName,
                            Collection<IEvent> outputEvents) {

        for (Object key : keys) {
            IEvent event = null;
            if (puts != null) {
                event = puts.remove(key);
            }

            if (tryPuts != null) {
                event = tryPuts.remove(key);
            }

            if (event != null) {
                setEventAttributes(event, sinkName, key);
                outputEvents.add(event);
            }
        }
    }

    private void refuseKeysInSink(Collection<Object> keys, ISink sink, Collection<IEvent> outputEvents) {
        Map<Object, IEvent> puts = this.puts.get(sink);
        Map<Object, IEvent> tryPuts = this.tryPuts.get(sink);

        String sinkName = manager.resolveObjectName(sink);
        refuseKeys(keys, puts, tryPuts, sinkName, outputEvents);

        if (puts != null && puts.isEmpty()) {
            this.puts.remove(sink);
        }

        if (tryPuts != null && tryPuts.isEmpty()) {
            this.tryPuts.remove(sink);
        }
    }
}
