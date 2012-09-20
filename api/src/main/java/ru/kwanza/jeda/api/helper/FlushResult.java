package ru.kwanza.jeda.api.helper;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.ISink;
import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.toolbox.fieldhelper.FieldHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Guzanov Alexander
 */
public class FlushResult {

    protected Map<ISink, Entry> results = new HashMap<ISink, Entry>();

    public Entry get(ISink sink) {
        return results.get(sink);
    }

    public boolean hasErrors() {
        for (Entry e : results.values()) {
            if (e.hasError()) {
                return true;
            }
        }

        return false;
    }

    public boolean hasDeclines() {
        for (Entry e : results.values()) {
            if (e.hasDeclines()) {
                return true;
            }
        }

        return false;
    }

    public boolean hasErrors(String... name) {
        for (String s : name) {
            if (results.get(s).hasError()) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public <E extends IEvent> Collection<E> getDeclines() {
        final Collection<E> result = new ArrayList<E>();
        for (Entry e : results.values()) {
            if (e.hasError() || e.hasDeclines()) {
                result.addAll((Collection<E>) e.getDeclines());
            }
        }
        return result;
    }

    public Map<ISink, Collection<IEvent>> getDeclinesMapBySink() {
        return FieldHelper.getValueFieldMap(results, Entry.DECLINES);
    }

    public static class Entry {
        private ISink sink;
        private SinkException e;
        private Collection<IEvent> declines;

        public static final FieldHelper.Field<Entry, Collection<IEvent>> DECLINES = new FieldHelper.Field<Entry, Collection<IEvent>>() {
            public Collection<IEvent> value(Entry object) {
                return object.getDeclines() ;
            }
        };


        public Entry(ISink sink) {
            this.sink = sink;
        }

        Entry(ISink sinkName, Collection<IEvent> declines) {
            this.sink = sinkName;
            this.declines = declines;
        }

        Entry(ISink sink, SinkException e) {
            this.sink = sink;
            this.e = e;
        }

        Entry(ISink sink, Collection<IEvent> declines, SinkException e) {
            this.sink = sink;
            this.declines = declines;
            this.e = e;
        }

        public ISink getSink() {
            return sink;
        }

        public SinkException getException() {
            return e;
        }

        public <E extends IEvent> Collection<E> getDeclines() {
            return (Collection<E>) declines;
        }

        public boolean hasError() {
            return e != null;
        }

        public boolean hasDeclines() {
            return declines != null && !declines.isEmpty();
        }
    }
}
