package ru.kwanza.jeda.clusterservice.impl.db;

import ru.kwanza.jeda.clusterservice.IClusterService;
import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.Node;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author Alexander Guzanov
 */
public class TestComponent implements IClusteredComponent {
    private String name;
    private volatile boolean started;
    private volatile boolean stopped;

    @Resource(name = "jeda.clusterservice.DBClusterService")
    private IClusterService service;

    public TestComponent(String name) {
        this.name = name;
    }

    @PostConstruct
    public void init() {
        service.registerComponent(this);
    }

    public String getName() {
        return name;
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

    public boolean handleStartRepair(Node node) {
        return false;
    }

    public void handleStopRepair(Node node) {

    }
}
