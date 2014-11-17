package ru.kwanza.jeda.clusterservice.impl.db;

import org.springframework.beans.factory.annotation.Autowired;
import ru.kwanza.jeda.clusterservice.IClusterService;
import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.Node;

import javax.annotation.PostConstruct;
import java.util.Timer;

/**
 * @author Alexander Guzanov
 */
public class TestComponent implements IClusteredComponent {
    private String name;
    @Autowired
    private IClusterService service;

    private static final Timer timer = new Timer(true);

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
    }

    public void handleStop() {

    }

    public void handleStartRepair(final Node node) {
//        final TestComponent self = this;
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                service.markRepaired(self, node);
//            }
//        }, 60000);

    }

    public void handleStopRepair(Node node) {

    }
}
