package ru.kwanza.jeda.nio.client;

import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.strategies.SameThreadIOStrategy;
import ru.kwanza.jeda.api.IFlowBus;
import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.api.internal.IStageInternal;
import ru.kwanza.jeda.api.internal.ISystemManager;
import ru.kwanza.jeda.core.manager.ObjectNotFoundException;
import ru.kwanza.jeda.core.stage.Stage;
import ru.kwanza.jeda.core.threadmanager.shared.SharedThreadManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

/**
 * @author Guzanov Alexander
 */
public class ClientTransportFlowBus implements IFlowBus<ITransportEvent> {
    private ISystemManager manager;
    private IDirectionQueueFactory directionQueueFactory;
    private TCPNIOTransport transport;
    private String name;
    private ConnectionPoolRegistry registry;
    private SharedThreadManager threadManager;
    private int threadCount = Runtime.getRuntime().availableProcessors();
    private IConnectionPoolConfigurator connectionPoolConfigurator;

    public void init() {
        if (directionQueueFactory == null) {
            directionQueueFactory = new DefaultDirectionQueueFactory();
        }
        if (transport == null) {
            TCPNIOTransportBuilder tcpTransportBuilder = TCPNIOTransportBuilder.newInstance();
            tcpTransportBuilder.setIOStrategy(SameThreadIOStrategy.getInstance());
            tcpTransportBuilder.setName("TCPNIOTransport-" + name);
            transport = tcpTransportBuilder.build();
            try {
                transport.start();
            } catch (IOException e) {
                throw new RuntimeException("Could not initialize ClientTransportFlowBus(" + name + ")!", e);
            }
        }
        registry = new ConnectionPoolRegistry(connectionPoolConfigurator != null ?
                connectionPoolConfigurator : new DefaultConnectionPoolConfigurator());
        threadManager = new SharedThreadManager(name, manager);
        threadManager.setStageComparator(new WaitingForConnectComparator());
        threadManager.setMaxThreadCount(threadCount);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setManager(ISystemManager manager) {
        this.manager = manager;
    }

    public void setDirectionQueueFactory(IDirectionQueueFactory directionQueueFactory) {
        this.directionQueueFactory = directionQueueFactory;
    }

    public void setTransport(TCPNIOTransport transport) {
        this.transport = transport;
    }

    public void setConnectionPoolConfigurator(IConnectionPoolConfigurator connectionPoolConfigurator) {
        this.connectionPoolConfigurator = connectionPoolConfigurator;
    }

    public void put(Collection<ITransportEvent> events) throws SinkException {
        Map<InetSocketAddress, Collection<ITransportEvent>> split = split(events);
        for (Map.Entry<InetSocketAddress, Collection<ITransportEvent>> e : split.entrySet()) {
            InetSocketAddress endpoint = e.getKey();
            String stageName = getStageName(endpoint);
            IStageInternal stage = null;
            try {
                stage = manager.getStageInternal(stageName);
            } catch (ObjectNotFoundException ex) {
            }

            if (stage == null) {
                stage = createDirectionStage(stageName, endpoint, manager);
                manager.registerStage(stage);
                stage = manager.getStageInternal(stageName);
            }

            stage.<ITransportEvent>getSink().put(e.getValue());
        }
    }

    private IStageInternal createDirectionStage(String stageName, InetSocketAddress endpoint, ISystemManager manager) {
        ConnectionPool connectionPool = registry.getConnectionPool(endpoint);
        return new Stage(manager, stageName,
                new EventProcessor(transport, connectionPool),
                directionQueueFactory.create(manager, endpoint),
                threadManager, null, connectionPool, false);
    }

    private String getStageName(InetSocketAddress key) {
        return this.name + key.toString();
    }

    public Collection<ITransportEvent> tryPut(Collection<ITransportEvent> events) throws SinkException {
        LinkedList<ITransportEvent> result = new LinkedList<ITransportEvent>();
        Map<InetSocketAddress, Collection<ITransportEvent>> split = split(events);
        for (Map.Entry<InetSocketAddress, Collection<ITransportEvent>> e : split.entrySet()) {
            InetSocketAddress endpoint = e.getKey();
            String stageName = getStageName(endpoint);
            IStageInternal stage = null;
            try {
                stage = manager.getStageInternal(stageName);
            } catch (ObjectNotFoundException ex) {
            }

            if (stage == null) {
                stage = createDirectionStage(stageName, endpoint, manager);
                manager.registerStage(stage);
                stage = manager.getStageInternal(stageName);
            }

            Collection<ITransportEvent> decline = stage.<ITransportEvent>getSink().tryPut(e.getValue());
            if (decline != null && !decline.isEmpty()) {
                result.addAll(decline);
            }
        }

        return result.isEmpty() ? null : result;
    }

    private Map<InetSocketAddress, Collection<ITransportEvent>> split(Collection<ITransportEvent> events) {
        Map<InetSocketAddress, Collection<ITransportEvent>> result =
                new HashMap<InetSocketAddress, Collection<ITransportEvent>>();
        for (ITransportEvent e : events) {
            InetSocketAddress endpoint = e.getConnectionConfig().getEndpoint();
            Collection<ITransportEvent> collection = result.get(endpoint);
            if (collection == null) {
                collection = new ArrayList<ITransportEvent>();
                result.put(endpoint, collection);
            }
            collection.add(e);
        }

        return result;
    }
}
