package ru.kwanza.jeda.core.threadmanager.stage;

import ru.kwanza.jeda.api.internal.IStageInternal;
import ru.kwanza.jeda.api.ISystemManager;
import ru.kwanza.jeda.api.internal.ISystemManagerInternal;
import ru.kwanza.jeda.core.threadmanager.AbstractProcessingThread;

/**
 * @author Guzanov Alexander
 */
class StageProcessingThread extends AbstractProcessingThread<StageThreadManager> {
    private IStageInternal stage;

    public StageProcessingThread(String name, ISystemManagerInternal manager,
                                 StageThreadManager threadManager,
                                 IStageInternal stage) {
        super(name, manager, threadManager);
        this.stage = stage;
    }

    public IStageInternal getStage() {
        if (!stage.getQueue().isReady()) {
            return null;
        }
        return stage;
    }
}
