package ru.kwanza.jeda.api.internal;

/**
 * @author Guzanov Alexander
 */
public abstract class AbstractResourceController implements IResourceController {
    private IStageInternal stage;

    public IStageInternal getStage() {
        return stage;
    }

    public final void initStage(IStageInternal stage) {
        if (this.stage != null) {
            throw new RuntimeException("Resource controller conform only one stage : " + stage.getName());
        }
        this.stage = stage;
    }
}
