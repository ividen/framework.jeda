package ru.kwanza.jeda.nio.server.http;

import ru.kwanza.jeda.nio.utils.IEntryPointKeystore;

/**
 * @author Guzanov Alexander
 */
public interface IEntryPoint {
    public String getHost();

    public int getPort();

    public String getName();

    public boolean useAJP();

    public int getServerConnectionBacklog();

    public long getConnectionIdleTimeout();

    public int getServerSocketSoTimeout();

    public IEntryPointKeystore getKeystore();
}
