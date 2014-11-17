package ru.kwanza.jeda.clusterservice.impl.db;

import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.jeda.clusterservice.impl.db.orm.AlienComponent;
import ru.kwanza.jeda.clusterservice.impl.db.orm.BaseComponentEntity;

/**
 * @author Alexander Guzanov
 */
public class ComponentHandler implements IClusteredComponent {
    private ComponentRepository repository;
    private IClusteredComponent delegate;

    public ComponentHandler(ComponentRepository repository, IClusteredComponent delegate) {
        this.repository = repository;
        this.delegate = delegate;
    }

    public ComponentHandler(ComponentRepository repository,String name) {
        this(repository,repository.getComponent(name));
    }

    public String getName() {
        return delegate.getName();
    }

    public void handleStart() {
        delegate.handleStart();
    }

    public void handleStop() {
        delegate.handleStop();
    }

    public void handleStartRepair(Node node) {
        delegate.handleStartRepair(node);
    }

    public void handleStopRepair(Node node) {
        delegate.handleStopRepair(node);
        final AlienComponent alienComponent = repository.getAlienEntities().get(BaseComponentEntity.createId(node, this));
        if(alienComponent!=null) {
            repository.addStopRepair(alienComponent);
        }
    }
}
