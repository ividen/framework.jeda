package ru.kwanza.jeda.nio.server;

import ru.kwanza.jeda.nio.server.http.IEntryPoint;
import ru.kwanza.jeda.nio.utils.IEntryPointKeystore;
import ru.kwanza.jeda.nio.server.http.IHttpServer;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

/**
 * @author Guzanov Alexander
 */
public class CustomKeystore implements IEntryPointKeystore {

    public void initServer(IHttpServer server, IEntryPoint entryPoint) {
        System.out.println("Init CustomKeystore " + server.getName() + " ,entry_point=" + entryPoint.getName());
    }

    public void initClient(String url) {

    }

    public void destroy() {
        System.out.println("Destroy CustomKeystore");
    }

    public TrustManager[] getTrustManagers() {
        return null;
    }

    public KeyManager[] getKeyManager() {
        return null;
    }
}
