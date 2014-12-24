package ru.kwanza.jeda.timerservice.pushtimer.consuming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Phased;
import org.springframework.stereotype.Component;
import ru.kwanza.jeda.clusterservice.IClusterService;
import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.jeda.timerservice.pushtimer.PendingUpdatesTimeRepository;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClassRepository;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClass;
import ru.kwanza.jeda.timerservice.pushtimer.consuming.supervisor.ConsumerSupervisorStageManager;
import ru.kwanza.jeda.timerservice.pushtimer.consuming.supervisor.ConsumerSupervisorThread;
import ru.kwanza.jeda.timerservice.pushtimer.memory.FiredTimersStorageRepository;
import ru.kwanza.jeda.timerservice.pushtimer.monitoring.EventStatistic;
import ru.kwanza.jeda.timerservice.pushtimer.monitoring.JMXRegistry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Michael Yeskov
 */
@Component
public class ConsumerComponent implements IClusteredComponent, Phased {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerComponent.class);

    @Resource( name = "jeda.clusterservice.DBClusterService")
    private IClusterService clusterService;
    @Resource
    private ConsumerSupervisorStageManager consumerSupervisorStageManager;
    @Resource
    private TimerClassRepository timerClassRepository;
    @Resource
    private FiredTimersStorageRepository firedTimersStorageRepository;
    @Resource
    private PendingUpdatesTimeRepository pendingUpdatesTimeRepository;
    @Resource
    private JMXRegistry jmxRegistry;

    @Resource(name ="timerservice.stats.fetch")
    private EventStatistic fetchStats;



    private ConcurrentHashMap<NodeId, AtomicLong> nodeStartupPointCount = new ConcurrentHashMap <NodeId, AtomicLong>();

    private ConcurrentHashMap<NodeId, Map <TimerClass, ConsumerSupervisorThread>> consumers = new ConcurrentHashMap<NodeId, Map<TimerClass, ConsumerSupervisorThread>>();

    @PostConstruct
    public void init() {
        clusterService.registerComponent(this);
    }

    @Override
    public String getName() {
        return "TimerConsumer";
    }

    @Override
    public void handleStart() {
        NodeId nodeId = new NodeId(clusterService.getCurrentNode().getId(), null);
        start(nodeId);
    }

    @Override
    public void handleStop() {
        stop(new NodeId(clusterService.getCurrentNode().getId(), null));
    }

    @Override
    public void handleStartRepair(Node node) {
        start(new NodeId(clusterService.getCurrentNode().getId(), node.getId()));
    }

    @Override
    public void handleStopRepair(Node node) {
        stop(new NodeId(clusterService.getCurrentNode().getId(), node.getId()));
    }

    private void start(NodeId nodeId) {
        logger.info("Component start enter");

        long pointCount = incrementStartupPointCount(nodeId);
        firedTimersStorageRepository.resumeProcessing(nodeId.getEffectiveNodeId(), pointCount);

        Map<TimerClass, ConsumerSupervisorThread> currentNodeMap = consumers.get(nodeId);
        if (currentNodeMap != null) {
            throw  new IllegalStateException(nodeId + "is already started");
        }
        currentNodeMap = new HashMap<TimerClass, ConsumerSupervisorThread>();


        for (TimerClass timerClass : timerClassRepository.getRegisteredTimerClasses()) {
            ConsumerSupervisorThread timerConsumer = new ConsumerSupervisorThread(nodeId, timerClass, pendingUpdatesTimeRepository, consumerSupervisorStageManager, firedTimersStorageRepository, jmxRegistry, fetchStats);
            currentNodeMap.put(timerClass, timerConsumer);
            timerConsumer.start();
        }

        consumers.put(nodeId, currentNodeMap);

        logger.info("Component start exit");
    }

    private long incrementStartupPointCount(NodeId nodeId) {
        AtomicLong pointCount = nodeStartupPointCount.get(nodeId);
        if (pointCount == null) {
            pointCount  = new AtomicLong(0);
            nodeStartupPointCount.put(nodeId, pointCount);
        }
        return pointCount.incrementAndGet();
    }

    private void stop(NodeId nodeId) {
        logger.info("Component stop enter");

        Map<TimerClass, ConsumerSupervisorThread> currentNodeMap = consumers.get(nodeId);
        if (currentNodeMap == null) {
            throw new IllegalStateException(nodeId + " is not started");
        }
        for (ConsumerSupervisorThread supervisorThread : currentNodeMap.values()) {
            supervisorThread.stopAndJoin();
        }
        consumers.remove(nodeId);

        firedTimersStorageRepository.stopProcessingAndWait(nodeId.getEffectiveNodeId());

        logger.info("Component stop exit");
    }


    @Override
    public int getPhase() { //after all timer classes register
        return 100;
    }

}
