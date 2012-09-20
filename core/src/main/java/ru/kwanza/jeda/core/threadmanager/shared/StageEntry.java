package ru.kwanza.jeda.core.threadmanager.shared;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.IEventProcessor;
import ru.kwanza.jeda.api.ISink;
import ru.kwanza.jeda.api.internal.*;

/**
 * @author Guzanov Alexander
 */
public class StageEntry {
    private IStageInternal stage;
    private StageWrapper wrapper;
    private long ts = System.currentTimeMillis();

    private int threadCount;
    private int currentThreadCount;


    public final class StageWrapper implements IStageInternal {
        public IThreadManager getThreadManager() {
            return stage.getThreadManager();
        }

        public IQueue getQueue() {
            return stage.getQueue();
        }

        public IAdmissionController getAdmissionController() {
            return stage.getAdmissionController();
        }

        public IEventProcessor getProcessor() {
            return stage.getProcessor();
        }

        public boolean hasTransaction() {
            return stage.hasTransaction();
        }

        public IResourceController getResourceController() {
            return stage.getResourceController();
        }

        public String getName() {
            return stage.getName();
        }

        public <E extends IEvent> ISink<E> getSink() {
            return stage.getSink();
        }

        protected StageEntry entry() {
            return StageEntry.this;
        }
    }

    protected StageEntry(IStageInternal stage) {
        this.stage = stage;
        this.wrapper = new StageWrapper();
    }

    protected IStageInternal wrapper() {
        return wrapper;
    }

    public final int getThreadCount() {
        return threadCount;
    }

    public final int getCurrentThreadCount() {
        return currentThreadCount;
    }

    public final void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public final long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public IStageInternal getStage() {
        return stage;
    }

    int decreaseCurrentThreadCount() {
        return --currentThreadCount;
    }

    int increaseCurrentThreadCount() {
        return ++currentThreadCount;
    }

    public boolean equals(Object obj) {
        if (obj instanceof IStageInternal) {
            return stage.equals(obj);
        }
        if (obj instanceof StageEntry) {
            return stage.equals(((StageEntry) obj).stage);
        }
        return super.equals(obj);
    }

    public int hashCode() {
        return stage.hashCode();
    }
}
