package ru.kwanza.jeda.nio.server.http;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

/**
 * @author Guzanov Alexander
 */
public interface IEntryPointKeystore {
    public void init(IHttpServer server, IEntryPoint entryPoint);

    public void destroy();

    public TrustManager[] getTrustManagers();

    public KeyManager[] getKeyManager();
}
