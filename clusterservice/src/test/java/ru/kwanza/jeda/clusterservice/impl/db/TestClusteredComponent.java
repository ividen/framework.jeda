package ru.kwanza.jeda.clusterservice.impl.db;

import ru.kwanza.jeda.clusterservice.IClusterService;
import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.Node;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author Alexander Guzanov
 */
public class TestClusteredComponent implements IClusteredComponent {
    int counter = 10;

    @Resource(name="jeda.clusterservice.DBClusterService")
    private IClusterService dbService;

    @PostConstruct
    public void init() {
        dbService.registerComponent(this);
    }

    public String getName() {
        return "TestClusteredModule";
    }

    public void handleStart() {
        System.out.println("Started ");
    }

    public void handleStop() {
        System.out.println("Stopped ");
    }

    public boolean handleStartRepair(Node node) {
        System.out.println("Repair " + counter + " of node " + node);
        return counter-- <= 0;
    }

    public void handleStopRepair(Node node) {

    }
}
