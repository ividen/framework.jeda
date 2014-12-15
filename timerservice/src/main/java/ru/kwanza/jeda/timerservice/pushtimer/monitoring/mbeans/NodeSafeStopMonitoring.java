package ru.kwanza.jeda.timerservice.pushtimer.monitoring.mbeans;

import org.springframework.stereotype.Component;
import ru.kwanza.jeda.timerservice.pushtimer.memory.FiredTimersStorageRepository;
import ru.kwanza.jeda.timerservice.pushtimer.monitoring.JMXRegistry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Michael Yeskov
 */
@Component
public class NodeSafeStopMonitoring extends AbstractCachedMonitoring implements NodeSafeStopMonitoringMBean {

    //cache
    private List<String> processingStoppedNodes = new ArrayList<String>();
    private Map<String, Long> nodeActiveProcessorsCount = new HashMap<String, Long>();
    private Map<String, Long> nodeStartupPointCount = new HashMap<String, Long>();

    @Resource
    private FiredTimersStorageRepository patient;
    @Resource
    private JMXRegistry jmxRegistry;

    @PostConstruct
    public void init() {
        jmxRegistry.registerInTotal(this.getClass().getSimpleName(), this);
    }


    @Override
    public List<String> getLive1ProcessingStoppedNodes() {
        return getFromCache(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                return processingStoppedNodes;
            }
        });
    }

    @Override
    public Map<String, Long> getLive2NodeActiveProcessorsCount() {
        return getFromCache(new Callable<Map<String, Long>>() {
            @Override
            public Map<String, Long> call() throws Exception {
                return nodeActiveProcessorsCount;
            }
        });
    }

    @Override
    public Map<String, Long> getLive3NodeStartupPointCount() {
        return getFromCache(new Callable<Map<String, Long>>() {
            @Override
            public Map<String, Long> call() throws Exception {
                return nodeStartupPointCount;
            }
        });
    }

    @Override
    protected void fillCache() {
        processingStoppedNodes.clear();
        nodeActiveProcessorsCount.clear();
        patient.lock();
        try {
            for (Long nodeId : patient.getProcessingStoppedBucketIds()) {
                processingStoppedNodes.add("Node" + nodeId);
            }
            for (Map.Entry<Long, AtomicLong> entry : patient.getBucketActiveProcessCount().entrySet()) {
                nodeActiveProcessorsCount.put("Node"+ entry.getKey(), entry.getValue().get());
            }

            for (Map.Entry<Long, Long> entry : patient.getBucketStartupPointCount().entrySet()) {
                nodeStartupPointCount.put("Node" + entry.getKey(), entry.getValue());
            }
        } finally {
            patient.unlock();
        }
    }


}
