package ru.kwanza.jeda.nio.client;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import ru.kwanza.jeda.api.internal.AbstractResourceController;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Guzanov Alexander
 */
class ConnectionPool extends AbstractResourceController {
    private AtomicInteger batchSize;
    private ConcurrentMap<Connection, ConnectionHolder> leasedConnections = new ConcurrentHashMap<Connection, ConnectionHolder>();
    private LinkedBlockingQueue<ConnectionHolder> availableConnections = new LinkedBlockingQueue<ConnectionHolder>();
    private InetSocketAddress address;
    private IConnectionPoolConfigurator configurator;
    private volatile int maxBatchSize;

    private static Attribute CONNECTION_POOL = Grizzly.DEFAULT_ATTRIBUTE_BUILDER.createAttribute("jeda-nio:ConnectionPool");

    public ConnectionPool(InetSocketAddress address, IConnectionPoolConfigurator configurator) {
        this.address = address;
        this.configurator = configurator;
        this.maxBatchSize = configurator.getPoolSize(address);
        this.batchSize = new AtomicInteger(maxBatchSize);
    }

    public void setMaxPoolSize(int maxPoolSize) {
        int delta = maxPoolSize - maxBatchSize;
        this.batchSize.addAndGet(delta);
    }

    public void throughput(int count, int batchSize, long millis, boolean success) {
    }

    public void input(long count) {
    }

    public double getInputRate() {
        return 0;
    }

    public double getThroughputRate() {
        return 0;
    }

    public int getBatchSize() {
        int size = batchSize.get();
        return size < 0 ? 0 : size;
    }

    public int getThreadCount() {
        return 1;
    }

    public void write(TCPNIOTransport transport, ITransportEvent event) {
        ConnectionConfig connectionConfig = event.getConnectionConfig();
        if (connectionConfig.isKeepAlive()) {
            ConnectionHolder holder = availableConnections.poll();
            if (holder != null && !holder.isTimedOut()) {
                registerConnectionHolder(holder, event, configurator.getKeepAliveTimeout(address));
                holder.getConnection().write(event.getContent());
                batchSize.decrementAndGet();
                return;
            } else if (holder != null) {
                batchSize.decrementAndGet();
                leasedConnections.remove(holder.getConnection());
                holder.getConnection().closeSilently();
            }
        }
        int i = batchSize.decrementAndGet();
        transport.connect(connectionConfig.getEndpoint(), new ConnectCompletionHandler(this, event));
    }

    void registerConnection(Connection connection, ITransportEvent event) {
        registerConnectionHolder(new ConnectionHolder(connection, event.getConnectionConfig(), configurator.getMaxRequestCount(address)),
                event, configurator.getKeepAliveTimeout(address));
    }

    void registerConnectionHolder(ConnectionHolder holder, ITransportEvent event, long keepAliveTimeout) {
        holder.update(keepAliveTimeout);
        Connection connection = holder.getConnection();
        ConnectionContext context = ConnectionContext.getContext(connection);
        if (context == null) {
            ConnectionContext.create(connection, event);
        } else {
            context.setRequestEvent(event);
        }
        leasedConnections.put(connection, holder);
        CONNECTION_POOL.set(connection, this);
    }

    void releaseConnection(Connection result) {
        ConnectionContext context = ConnectionContext.getContext(result);

        if (context != null) {
            try {
                if (context.getRequestEvent().getConnectionConfig().isKeepAlive()) {
                    returnConnection(result, context.getRequestEvent().getConnectionConfig());
                } else {
                    result.closeSilently();
                }
            } finally {
               context.clear();
            }
        }
    }

    void returnConnection(Connection result, ConnectionConfig config) {
        returnConnection(result, config, false);
    }

    void returnConnection(Connection result, ConnectionConfig config, boolean close) {
        if (result != null) {
            ConnectionHolder holder = leasedConnections.remove(result);
            if (config.isKeepAlive()) {
                if (holder != null && result.isOpen() && !close) {
                    availableConnections.offer(holder);
                    getStage().getThreadManager().adjustThreadCount(getStage(), getThreadCount());
                }
            }
            batchSize.incrementAndGet();
        }
    }

    public static ConnectionPool getPool(Connection connection) {
        return (ConnectionPool) CONNECTION_POOL.get(connection);
    }
}
