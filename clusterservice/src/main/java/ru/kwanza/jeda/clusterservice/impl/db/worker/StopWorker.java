package ru.kwanza.jeda.clusterservice.impl.db.worker;

import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.impl.db.worker.AbstractWorker;

/**
* @author Alexander Guzanov
*/
public class StopWorker extends AbstractWorker {

    public StopWorker(IClusteredComponent component) {
        super(component);
    }

    @Override
    protected void work(IClusteredComponent component) {
        component.handleStop();
    }
}
