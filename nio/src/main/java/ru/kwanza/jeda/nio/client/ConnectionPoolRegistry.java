package ru.kwanza.jeda.nio.client;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Guzanov Alexander
 */
class ConnectionPoolRegistry {
    private ConcurrentMap<InetSocketAddress, ConnectionPool> registry
            = new ConcurrentHashMap<InetSocketAddress, ConnectionPool>();
    private IConnectionPoolConfigurator configurator;

    ConnectionPoolRegistry(IConnectionPoolConfigurator configurator) {
        this.configurator = configurator;
    }


    public ConnectionPool getConnectionPool(InetSocketAddress address) {
        ConnectionPool result = registry.get(address);
        if (result == null) {
            result = createPool(address);
            ConnectionPool putIfAbsent = registry.putIfAbsent(address, result);
            if (putIfAbsent != null) {
                result = putIfAbsent;
            }
        }

        return result;
    }

    public int getSize() {
        return registry.size();
    }

    private ConnectionPool createPool(InetSocketAddress address) {
        return new ConnectionPool(address, configurator);
    }

    public void setConnectionPoolSize(InetSocketAddress address, int size) {
        getConnectionPool(address).setMaxPoolSize(size);
    }
}
