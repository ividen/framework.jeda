package ru.kwanza.jeda.clusterservice.impl.db;

import ru.kwanza.jeda.clusterservice.IClusterService;
import ru.kwanza.jeda.clusterservice.IClusteredModule;
import ru.kwanza.jeda.clusterservice.Node;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author Alexander Guzanov
 */
public class TestModule implements IClusteredModule {
    private String name;
    private volatile boolean started;
    private volatile boolean stopped;

    @Resource(name = "jeda.clusterservice.DBClusterService")
    private IClusterService service;

    public TestModule(String name) {
        this.name = name;
    }

    @PostConstruct
    public void init() {
        service.registerModule(this);
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

    public boolean handleRepair(Node node) {
        return false;
    }
}
