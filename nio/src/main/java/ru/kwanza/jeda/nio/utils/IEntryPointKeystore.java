package ru.kwanza.jeda.nio.utils;

import ru.kwanza.jeda.nio.server.http.IEntryPoint;
import ru.kwanza.jeda.nio.server.http.IHttpServer;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

/**
 * @author Guzanov Alexander
 */
public interface IEntryPointKeystore {
    public void initServer(IHttpServer server, IEntryPoint entryPoint);

    public void initClient(String url);

    public void destroy();

    public TrustManager[] getTrustManagers();

    public KeyManager[] getKeyManager();
}
