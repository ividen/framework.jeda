package ru.kwanza.jeda.clusterservice.impl.db.worker;

import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.jeda.clusterservice.impl.db.worker.AbstractWorker;

/**
* @author Alexander Guzanov
*/
public class StartRepairWorker extends AbstractWorker {
    private Node node;

    public StartRepairWorker(IClusteredComponent component, Node node) {
        super(component);
        this.node = node;
    }


    @Override
    protected void work(IClusteredComponent component) {
        component.handleStartRepair(node);
    }
}
