package ru.kwanza.jeda.nio.client;

import java.net.InetSocketAddress;

/**
 * //todo aguzanov реализовать файловый конфигуратор
 *
 * @author Guzanov Alexander
 */
public interface IConnectionPoolConfigurator {

    public int getPoolSize(InetSocketAddress address);

    public long getKeepAliveTimeout(InetSocketAddress address);

    public long getMaxRequestCount(InetSocketAddress address);
}
