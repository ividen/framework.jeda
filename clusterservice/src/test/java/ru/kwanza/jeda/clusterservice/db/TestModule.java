package ru.kwanza.jeda.clusterservice.db;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Resource(name="jeda.clusterservice.DBClusterService")
    private IClusterService service;

    public TestModule(String name) {
        this.name = name;
    }

    @PostConstruct
    public void init(){
        service.registerModule(this);
    }

    public String getName() {
        return name;
    }

    public void handleStart() {

    }

    public void handleStop() {

    }

    public boolean handleRepair(Node node) {
        return false;
    }
}
