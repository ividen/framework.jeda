package ru.kwanza.jeda.nio.client;

import java.net.InetSocketAddress;

/**
 * @author Guzanov Alexander
 */
public interface IConnectionPoolConfigurator {

    public int getPoolSize(InetSocketAddress address);

    public long getKeepAliveTimeout(InetSocketAddress address);

    public long getMaxRequestCount(InetSocketAddress address);
}
