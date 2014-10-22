package ru.kwanza.jeda.clusterservice.impl.db;

import ru.kwanza.jeda.clusterservice.IClusterService;
import ru.kwanza.jeda.clusterservice.IClusteredModule;
import ru.kwanza.jeda.clusterservice.Node;

import javax.annotation.PostConstruct;

/**
 * @author Alexander Guzanov
 */
public class RepairableTestModule implements IClusteredModule {
    private volatile boolean started;
    private volatile boolean stopped;
    private volatile boolean repairing = false;
    private volatile boolean repaired = false;

    private IClusterService service;

    public RepairableTestModule(IClusterService service) {
        this.service = service;
    }

    @PostConstruct
    public void init() {
        service.registerModule(this);
    }

    public String getName() {
        return "repairable_module";
    }

    public void handleStart() {
        this.started = true;
        this.stopped = false;
    }

    public void handleStop() {
        this.started = false;
        this.stopped = true;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isStopped() {
        return stopped;
    }

    public boolean isRepairing() {
        return repairing;
    }

    public boolean isRepaired() {
        return repaired;
    }

    public void setRepaired(boolean repaired) {
        this.repaired = repaired;
    }

    public boolean handleRepair(Node node) {
        repairing = true;
        return repaired;

    }
}
