package ru.kwanza.jeda.core.threadmanager.shared;

import ru.kwanza.jeda.api.internal.IStageInternal;
import ru.kwanza.jeda.api.internal.IJedaManagerInternal;
import ru.kwanza.jeda.core.threadmanager.AbstractProcessingThread;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Guzanov Alexander
 */
class SharedProcessingThread extends AbstractProcessingThread<SharedThreadManager> {

    SharedProcessingThread(String name, IJedaManagerInternal manager, SharedThreadManager threadManager) {
        super(name, manager, threadManager);
    }

    @Override
    public IStageInternal getStage() {
        StageEntry stageEntry = this.getThreadManager().findStageEntry();
        return stageEntry == null ? null : stageEntry.wrapper();
    }

    @Override
    protected boolean process(IStageInternal stage) {
        boolean result = false;
        try {
            result = super.process(stage);
            return result;
        } finally {
            if (result) {
                adjustStageThreadCount((StageEntry.StageWrapper) stage);
            }
        }
    }

    private void adjustStageThreadCount(StageEntry.StageWrapper stage) {
        StageEntry entry = stage.entry();

        ReentrantLock lock = getThreadManager().getLock();
        lock.lock();
        try {
            entry.decreaseCurrentThreadCount();
        } finally {
            lock.unlock();
        }
    }
}
