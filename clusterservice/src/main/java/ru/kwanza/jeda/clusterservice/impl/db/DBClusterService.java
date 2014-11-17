package ru.kwanza.jeda.clusterservice.impl.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import ru.kwanza.dbtool.core.UpdateException;
import ru.kwanza.jeda.clusterservice.IClusterService;
import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.jeda.clusterservice.impl.db.orm.AlienComponent;
import ru.kwanza.jeda.clusterservice.impl.db.orm.ComponentEntity;
import ru.kwanza.jeda.clusterservice.impl.db.orm.NodeEntity;
import ru.kwanza.toolbox.fieldhelper.FieldHelper;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Alexander Guzanov
 */
public class DBClusterService implements IClusterService, ApplicationListener<ContextRefreshedEvent>, Runnable {
    public static final String SUPERVISOR_NAME = "DBClusterService-Supervisor";
    public static final String WORKER_NAME = "DBClusterService-Worker";

    public static Logger logger = LoggerFactory.getLogger(DBClusterService.class);

    @Autowired
    private DBClusterServiceDao dao;
    @Autowired
    private ComponentRepository repository;
    @Autowired
    private WorkerController workers;

    private NodeEntity currentNode;
    private Integer currentNodeId;
    private volatile long lastActivityTs;

    private long failoverInterval;
    private long activityInterval;

    private volatile boolean started = false;
    private Thread supervisor;

    private Lock repairLock = new ReentrantLock();


    @PostConstruct
    public void init() {
        initCurrentNode();
        initSupervisors();
    }

    @PreDestroy
    public void destroy() throws InterruptedException, UpdateException {
        logger.info("Stopping {} ...", SUPERVISOR_NAME);
        started = false;
        supervisor.interrupt();
        supervisor.join(60000);
        updateActivity(repository.getActiveEntities(), 0);
        for (IClusteredComponent o : repository.getActiveComponents().values()) {
            try {
                o.handleStop();
            } catch (Throwable e) {
                logger.error("Error stopping " + o.getName(), e);
            }
        }
    }

    private void initCurrentNode() {
        currentNode = dao.findOrCreateNode(new NodeEntity(currentNodeId,
                System.currentTimeMillis(), getIPAddress(), getPID()));
    }

    private String getPID() {
        String fallback = "<PID>";
        String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        int index = jvmName.indexOf('@');

        if (index < 1) {
            return fallback;
        }

        try {
            return Long.toString(Long.parseLong(jvmName.substring(0, index)));
        } catch (NumberFormatException e) {
        }
        return fallback;
    }

    private String getIPAddress() {
        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            final InetAddress localHost = InetAddress.getLocalHost();
            while (e.hasMoreElements()) {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements()) {
                    InetAddress i = (InetAddress) ee.nextElement();
                    if ((i instanceof Inet4Address) && !localHost.equals(i)) {
                        return i.getHostAddress();
                    }
                }
            }
            return localHost.getHostAddress();
        } catch (Exception e) {
            throw new RuntimeException("Can't determine localhost ip address!", e);
        }
    }

    public Integer getCurrentNodeId() {
        return currentNodeId;
    }

    public long getFailoverInterval() {
        return failoverInterval;
    }

    public void setFailoverInterval(long failoverInterval) {
        this.failoverInterval = failoverInterval;
    }

    public long getActivityInterval() {
        return activityInterval;
    }

    public void setActivityInterval(long activityInterval) {
        this.activityInterval = activityInterval;
    }

    public void setCurrentNodeId(Integer currentNodeId) {
        this.currentNodeId = currentNodeId;
    }

    private void initSupervisors() {
        supervisor = new Thread(this, SUPERVISOR_NAME);
        supervisor.setDaemon(true);
    }

    public List<? extends Node> getActiveNodes() {
        return dao.selectActiveNodes();
    }

    public List<? extends Node> getPassiveNodes() {
        return dao.selectPassiveNodes();
    }

    public List<? extends Node> getNodes() {
        return dao.selectNodes();
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    public Map<String, IClusteredComponent> getRepository() {
        return repository.getComponents();
    }

    public Map<String, IClusteredComponent> getActiveComponents() {
        return repository.getActiveComponents();
    }

    public Map<String, IClusteredComponent> getPassiveComponents() {
        return repository.getPassiveComponents();
    }

    public <R> R criticalSection(IClusteredComponent component, Callable<R> callable)
            throws InvocationTargetException, ComponentInActiveExcetion {
        checkActivity(component);
        R result;
        try {
            result = callable.call();
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }

        checkActivity(component);
        return result;
    }

    private void checkActivity(IClusteredComponent component) throws ComponentInActiveExcetion {
        if (!repository.isActive(component.getName())) {
            throw new ComponentInActiveExcetion("Component " + component.getName() + " is inactive!");
        }
    }

    public boolean markRepaired(IClusteredComponent component, Node node) {
        repairLock.lock();
        try {
            AlienComponent componentEntity = repository.getAlienEntities().get(ComponentEntity.createId(node, component));
            if (componentEntity != null) {
                workers.stopRepair(componentEntity.getId(), new ComponentHandler(repository, component), node);
            }
        } finally {
            repairLock.unlock();
        }
        return true;
    }

    public void registerComponent(IClusteredComponent component) {
        if (started) {
            throw new IllegalStateException("Can't regiter component " + component.getName() +
                    "! Supervisor " + SUPERVISOR_NAME + "is already started!");
        }

        dao.findOrCreateComponent(currentNode, component);
        repository.registerComponent(component);
    }

    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.info("Starting {} ...", SUPERVISOR_NAME);
        supervisor.start();
        started = true;
    }


    public void run() {
        logger.info("Started {}", SUPERVISOR_NAME);

        initLoop();
        workLoop();

        logger.info("Stopped {}", SUPERVISOR_NAME);
    }

    private void workLoop() {
        while (started && !Thread.currentThread().isInterrupted()) {
            calcLastActivity();
            try {
                leaseCurrentNode();
                leaseActivity();
                checkPassiveComponents();
                handleAlienComponents();
                Thread.sleep(activityInterval);
            } catch (InterruptedException e) {
                break;
            } catch (Throwable e) {
                logger.error("Error in supervisor!", e);
                continue;
            }
        }
    }

    private void initLoop() {
        while (true) {
            try {
                calcLastActivity();
                processInitialState();
                break;
            } catch (Throwable ex) {
                continue;
            }
        }
    }

    private void leaseCurrentNode() {
        currentNode.setLastActivity(lastActivityTs);
        dao.updateNode(currentNode);
    }

    private void handleAlienComponents() {
        repairLock.lock();
        try {
            leaseAlien();
            finishStop();
            findStaleAlien();
        } finally {
            repairLock.unlock();
        }
    }

    private void finishStop() {
        if (!repository.getStopRepairEntities().isEmpty()) {

            final Collection<AlienComponent> items = repository.getStopRepairEntities().values();
            for (AlienComponent item : items) {
                item.clearMarkers();
                item.setRepaired(true);
                item.setLastActivity(0l);
            }

            try {
                dao.updateAlienComponents(items);
            } catch (UpdateException e) {
            }

            for (AlienComponent alienComponent : items) {
                repository.removeStopingRepairComponent(alienComponent.getId());
                repository.removeStopRepairComponent(alienComponent.getId());
                repository.removeAlienComponent(alienComponent.getId());
            }
        }
    }

    private void leaseAlien() {
        if (!repository.getAlienEntities().isEmpty()) {
            Collection<ComponentEntity> items = dao.loadComponentsByKey(repository.getAlienEntities().keySet());
            for (ComponentEntity item : items) {
                if (item.getWaitForReturn()) {
                    if (!repository.getStopigRepairEntities().containsKey(item.getId())) {
                        repository.addStopingRepair(repository.getAlienEntities().get(item.getId()));
                        workers.stopRepair(item.getId(), new ComponentHandler(repository, item.getName()), item.getNode());
                    }
                }
            }

            for (AlienComponent o : repository.getAlienEntities().values()) {
                o.setLastActivity(lastActivityTs);
                o.setHoldNodeId(currentNode.getId());
            }

            try {
                dao.updateAlienComponents(repository.getAlienEntities().values());
            } catch (UpdateException e) {
            }
        }
    }

    private void findStaleAlien() {
        List<AlienComponent> items = dao.selectAlienStaleComponents(currentNode,
                repository.getActiveComponents().keySet());

        for (AlienComponent item : items) {
            if(item.getRepaired()){
                throw new RuntimeException("BUG JOP!");
            }
        }

        List<AlienComponent> newItems = new ArrayList<AlienComponent>();
        final Map<String, AlienComponent> alienEntities = repository.getAlienEntities();
        for (AlienComponent component : items) {
            if (!alienEntities.containsKey(component.getId())) {
                component.clearMarkers();
                component.setLastActivity(lastActivityTs);
                component.setHoldNodeId(currentNodeId);
                newItems.add(component);
            }
        }

        try {
            dao.updateAlienComponents(newItems);
        } catch (UpdateException e) {
            newItems = e.getUpdated();
        }

        for (AlienComponent component : newItems) {
            repository.addAlientComponent(component);
            workers.startRepair(component.getId(), new ComponentHandler(repository, component.getName()), component.getNode());
        }
    }


    private void calcLastActivity() {
        lastActivityTs = System.currentTimeMillis() + failoverInterval;
    }

    private void processInitialState() {
        Collection<ComponentEntity> items = dao.selectComponents(currentNode, repository.getComponents().keySet());
        filterComponentByState(items);
        try {
            updateActivity(repository.getActiveEntities(),lastActivityTs);
        } catch (UpdateException e) {
            for (ComponentEntity o : e.<ComponentEntity>getConstrainted()) {
                repository.removeActiveComponent(o.getId());
                repository.addPassiveComponent(o);
            }
            for (ComponentEntity o : e.<ComponentEntity>getOptimistic()) {
                repository.removeActiveComponent(o.getId());
                repository.addPassiveComponent(o);
            }
        }
        startActiveComponents();
    }

    private void startActiveComponents() {
        for (ComponentEntity item : repository.getActiveEntities()) {
            workers.startComponent(item.getId(), new ComponentHandler(repository, item.getName()));
        }
    }

    private void filterComponentByState(Collection<ComponentEntity> items) {
        final long ts = System.currentTimeMillis();
        for (ComponentEntity item : items) {
            if (item.getHoldNodeId() != null) {
                if (item.getLastActivity() <= ts) {
                    item.clearMarkers();
                    repository.addActiveComponent(item);
                } else {
                    repository.addPassiveComponent(item);
                }
            } else {
                repository.addActiveComponent(item);
            }
        }
    }

    private void leaseActivity() {
        try {
            updateActivity(repository.getActiveEntities(), lastActivityTs);
        } catch (UpdateException e) {
            stopComponents(e.<ComponentEntity>getConstrainted());
            stopComponents(e.<ComponentEntity>getOptimistic());
        }
    }

    private void stopComponents(List<ComponentEntity> optimistic) {
        for (ComponentEntity o : optimistic) {
            repository.addPassiveComponent(o);
            if (repository.removeActiveComponent(o.getName())) {
                workers.stopComponent(o.getId(), new ComponentHandler(repository, o.getName()));
            }
        }
    }

    private void activateCandidates(Collection<ComponentEntity> activateCandidates) {
        try {
            updateActivity(activateCandidates, lastActivityTs);
        } catch (UpdateException e) {
            activateCandidates = e.getUpdated();
        }

        for (ComponentEntity item : activateCandidates) {
            repository.addActiveComponent(item);
            repository.removePassiveComponent(item.getName());
            workers.startComponent(item.getId(), new ComponentHandler(repository, item.getName()));
        }
    }


    private void updateActivity(Collection<ComponentEntity> items, long ts) throws UpdateException {
        for (ComponentEntity component : items) {
            component.setLastActivity(ts);
            component.clearMarkers();
        }

        dao.updateComponents(items);
    }

    private void checkPassiveComponents() {
        if (!repository.getPassiveEntities().isEmpty()) {
            Collection<ComponentEntity> items = dao.selectActivationCandidate(getPassiveEntitiesKeys());
            activateCandidates(items);
            dao.markWaitForReturn(repository.getPassiveEntities());
        }
    }

    private Collection<String> getPassiveEntitiesKeys() {
        return FieldHelper.getFieldCollection(repository.getPassiveEntities(),
                FieldHelper.<ComponentEntity, String>construct(ComponentEntity.class, "id"));
    }
}
