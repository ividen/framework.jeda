package ru.kwanza.jeda.core.threadmanager;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.IStage;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Guzanov Alexander
 */
public class ErrorController {
    private static final ErrorController instance = new ErrorController();
    private ConcurrentHashMap<IStage, ConcurrentHashMap<DangerousEntry, DangerousEntry>> errors
            = new ConcurrentHashMap<IStage, ConcurrentHashMap<DangerousEntry, DangerousEntry>>();
    private ReferenceQueue referenceQueue = new ReferenceQueue();

    public static class DangerousEntry extends WeakReference<IEvent> {
        private int attempts = 0;
        private int hashCode;
        private IStage stage;

        DangerousEntry(IEvent referent) {
            super(referent);
            this.hashCode = referent.hashCode();
        }

        DangerousEntry(IEvent referent, IStage stage, ReferenceQueue queue) {
            super(referent, queue);
            this.hashCode = referent.hashCode();
            this.stage = stage;
        }

        public int getAttempts() {
            return attempts;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DangerousEntry) {
                DangerousEntry entry = (DangerousEntry) obj;
                if (entry == this) {
                    return true;
                }

                IEvent event = entry.get();

                IEvent referent = get();
                if (referent == null || event == null) {
                    return false;
                }
                return referent.equals(event);
            }

            return false;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }

    public static ErrorController getInstance() {
        return instance;
    }

    private void clean() {
        for (; ; ) {
            DangerousEntry poll = (DangerousEntry) referenceQueue.poll();
            if (poll == null) {
                break;
            }

            ConcurrentHashMap<DangerousEntry, DangerousEntry> entries = errors.get(poll.stage);
            entries.remove(poll);
        }
    }

    public DangerousEntry findDangerousElement(IStage stage, IEvent event) {
        clean();
        ConcurrentHashMap<DangerousEntry, DangerousEntry> dangerousEntries = getEntries(stage);
        return dangerousEntries.get(new DangerousEntry(event));
    }

    public DangerousEntry registerDangerousElement(IStage stage, IEvent event) {
        clean();
        ConcurrentHashMap<DangerousEntry, DangerousEntry> dangerousEntries = getEntries(stage);
        DangerousEntry result = dangerousEntries.get(new DangerousEntry(event));
        if (result == null) {
            result = new DangerousEntry(event, stage, referenceQueue);
            if (dangerousEntries.putIfAbsent(result, result) != result) {
                result = dangerousEntries.get(result);
            }
        }

        result.attempts++;
        return result;
    }

    public void removeDangerousElement(IStage stage, IEvent event) {
        clean();
        ConcurrentHashMap<DangerousEntry, DangerousEntry> dangerousEntries = getEntries(stage);
        dangerousEntries.remove(new DangerousEntry(event));
    }

    private ConcurrentHashMap<DangerousEntry, DangerousEntry> getEntries(IStage stage) {
        ConcurrentHashMap<DangerousEntry, DangerousEntry> dangerousEntries = errors.get(stage);
        if (dangerousEntries == null) {
            dangerousEntries = new ConcurrentHashMap<DangerousEntry, DangerousEntry>();
            if (errors.putIfAbsent(stage, dangerousEntries) != dangerousEntries) {
                dangerousEntries = errors.get(stage);
            }
        }
        return dangerousEntries;
    }
}
