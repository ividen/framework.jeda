package ru.kwanza.jeda.clusterservice.impl.db;

import ru.kwanza.jeda.clusterservice.IClusterService;
import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.Node;

import javax.annotation.PostConstruct;

/**
 * @author Alexander Guzanov
 */
public class RepairableTestComponent implements IClusteredComponent {
    private volatile boolean started;
    private volatile boolean stopped;
    private volatile boolean repairing = false;
    private volatile boolean repaired = false;

    private IClusterService service;

    public RepairableTestComponent(IClusterService service) {
        this.service = service;
    }

    @PostConstruct
    public void init() {
        service.registerComponent(this);
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

    public boolean handleStartRepair(Node node) {
        repairing = true;
        return repaired;

    }

    public void handleStopRepair(Node node) {

    }
}
