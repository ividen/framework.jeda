package ru.kwanza.jeda.clusterservice.impl.db.worker;

import ru.kwanza.jeda.clusterservice.IClusteredComponent;

/**
* @author Alexander Guzanov
*/
public abstract class AbstractWorker implements Runnable {
    private IClusteredComponent component;

    public AbstractWorker(IClusteredComponent component) {
        this.component = component;
    }

    public void run() {
        while (true) {
            try {
                work(component);
                break;
            } catch (Throwable ex) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    break;
                }
                continue;
            }
        }
    }

    protected abstract void work(IClusteredComponent component);
}
