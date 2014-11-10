package ru.kwanza.jeda.clusterservice.impl.db.worker;

import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.impl.db.worker.AbstractWorker;

/**
* @author Alexander Guzanov
*/
public class StartWorker extends AbstractWorker {
    public StartWorker(IClusteredComponent component) {
        super(component);
    }

    @Override
    protected void work(IClusteredComponent component) {
        component.handleStart();
    }
}
