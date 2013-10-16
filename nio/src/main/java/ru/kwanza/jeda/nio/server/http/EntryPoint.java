package ru.kwanza.jeda.nio.server.http;

import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.http.HttpServerFilter;
import org.glassfish.grizzly.http.KeepAlive;
import org.glassfish.grizzly.http.ajp.AjpHandlerFilter;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.grizzly.ssl.SSLFilter;
import org.glassfish.grizzly.strategies.SameThreadIOStrategy;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.grizzly.utils.DelayedExecutor;
import ru.kwanza.jeda.nio.utils.IEntryPointKeystore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import static ru.kwanza.jeda.nio.server.http.HttpServer.logger;

/**
 * @author Guzanov Alexander
 */
public class EntryPoint implements IEntryPoint {
    private HttpServer server;

    private IEntryPointKeystore keystore;
    private Integer keepAliveIdleTimeout = null;
    private Integer keepAliveMaxRequestsCount = null;
    private Integer threadCount;
    private String host;
    private String name;
    private TCPNIOTransport transport;
    private boolean useAJP;
    private int port;
    private int serverConnectionBacklog = Const.DEFAULT_SERVER_CONNECTION_BACKLOG;
    private int serverSocketSoTimeout = Const.DEFUALT_SERVER_SOCKET_SO_TIMEOUT;
    private long connectionIdleTimeout = Const.DEFAULT_CONNECTION_IDLE_TIMEOUT;

    public EntryPoint(int port) {
        this(null, port);
    }

    public EntryPoint(String host, int port) {
        this.host = host;
        this.port = port;
        this.name = (host == null ? "*" : host) + ":" + port;
    }

    public int getServerConnectionBacklog() {
        return serverConnectionBacklog;
    }

    public long getConnectionIdleTimeout() {
        return connectionIdleTimeout;
    }

    public int getServerSocketSoTimeout() {
        return serverSocketSoTimeout;
    }

    public IEntryPointKeystore getKeystore() {
        return keystore;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    public boolean useAJP() {
        return useAJP;
    }

    public Integer getKeepAliveIdleTimeout() {
        return keepAliveIdleTimeout;
    }

    public void setKeepAliveIdleTimeout(Integer keepAliveIdleTimeout) {
        this.keepAliveIdleTimeout = keepAliveIdleTimeout;
    }

    public Integer getKeepAliveMaxRequestsCount() {
        return keepAliveMaxRequestsCount;
    }

    public void setKeepAliveMaxRequestsCount(Integer keepAliveMaxRequestsCount) {
        this.keepAliveMaxRequestsCount = keepAliveMaxRequestsCount;
    }

    public Integer getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(Integer threadCount) {
        this.threadCount = threadCount;
    }

    public void setConnectionIdleTimeout(long connectionIdleTimeout) {
        this.connectionIdleTimeout = connectionIdleTimeout;
    }

    public void setKeystore(IEntryPointKeystore keystore) {
        this.keystore = keystore;
    }

    public void setServerConnectionBacklog(int serverConnectionBacklog) {
        this.serverConnectionBacklog = serverConnectionBacklog;
    }

    public void setServerSocketSoTimeout(int serverSocketSoTimeout) {
        this.serverSocketSoTimeout = serverSocketSoTimeout;
    }

    public void setUseAJP(boolean useAJP) {
        this.useAJP = useAJP;
    }

    public SSLFilter createSSLFilter() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        KeyManager[] keyManager = keystore.getKeyManager();
        TrustManager[] trustManagers = keystore.getTrustManagers();
        sslContext.init(keyManager, trustManagers, null);
        boolean useClientAuthentication = trustManagers != null;
        SSLEngineConfigurator serverSSLEngineConfigurator = new SSLEngineConfigurator(sslContext, false,
                useClientAuthentication, useClientAuthentication);
        return new SSLFilter(serverSSLEngineConfigurator, null);
    }

    public void destroy() {
        try {
            transport.stop();
        } catch (IOException e) {
            logger.error("Error stoping transport for HttpServer(" + server.getName() + ":" + getName() + ")", e);
        }

        if (keystore != null) {
            keystore.destroy();
        }
    }

    void init(HttpServer server) {
        this.server = server;
        FilterChainBuilder serverFilterChainBuilder = FilterChainBuilder.stateless();

        serverFilterChainBuilder.add(new TransportFilter());
        if (useAJP()) {
            serverFilterChainBuilder.add(new AjpHandlerFilter());
        }

        if (keystore != null) {
            keystore.initServer(server, this);
            try {
                serverFilterChainBuilder.add(createSSLFilter());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        KeepAlive keepAlive = null;
        int keepAliveIdleTimeout = this.keepAliveIdleTimeout == null ?
                server.getKeepAliveIdleTimeout() : this.keepAliveIdleTimeout;
        int keepAliveMaxRequestsCount = this.keepAliveMaxRequestsCount == null ?
                server.getKeepAliveMaxRequestsCount() : this.keepAliveMaxRequestsCount;
        if (keepAliveIdleTimeout > 0 || keepAliveMaxRequestsCount > 0) {
            keepAlive = new KeepAlive();
            keepAlive.setIdleTimeoutInSeconds(server.getKeepAliveIdleTimeout());
            keepAlive.setMaxRequestsCount(server.getKeepAliveMaxRequestsCount());
        }

        serverFilterChainBuilder.add(new HttpServerFilter(true, HttpServerFilter.DEFAULT_MAX_HTTP_PACKET_HEADER_SIZE,
                keepAlive, keepAlive == null ? null : new DelayedExecutor(server.getExecutorService())));
        serverFilterChainBuilder.add(new ServerFilter(server, this));

        TCPNIOTransportBuilder transportBuilder = TCPNIOTransportBuilder.newInstance();
        transportBuilder.setIOStrategy(SameThreadIOStrategy.getInstance());
        ThreadPoolConfig selectorThreadPoolConfig = transportBuilder.getSelectorThreadPoolConfig();
        selectorThreadPoolConfig
                .setPoolName("HttpServer(" + server.getName() + ":" + this.getName() + ")");
        if (threadCount != null) {
            selectorThreadPoolConfig.setCorePoolSize(threadCount);
            selectorThreadPoolConfig.setMaxPoolSize(threadCount);
        }
        transportBuilder.setServerConnectionBackLog(serverConnectionBacklog);
        transportBuilder.setServerSocketSoTimeout(serverSocketSoTimeout);
        transport = transportBuilder.build();
        transport.setProcessor(serverFilterChainBuilder.build());

        if (logger.isInfoEnabled()) {
            logger.info("HttpServer({}:{}) Staring entry point at host={} " +
                    ", port={}, connectionIdleTimeout={}" +
                    ", serverConnectionBackLog={}, serverSocketSoTimeout={} , supportKeepAlive={}" +
                    ", keepAliveIdleTimeout={} , keepAliveMaxRequestsCount={} " +
                    ", threadCount={}", new Object[]{server.getName(), this.getName(), host, port,
                    connectionIdleTimeout, serverConnectionBacklog, serverSocketSoTimeout, keepAlive != null,
                    keepAlive == null ? 0 : keepAlive.getIdleTimeoutInSeconds(),
                    keepAlive == null ? 0 : keepAlive.getIdleTimeoutInSeconds(),
                    selectorThreadPoolConfig.getMaxPoolSize()});
        }
        try {
            if (host == null) {
                transport.bind(port);
            } else {
                transport.bind(host, port);
            }
            transport.start();
        } catch (IOException e) {
            throw new RuntimeException("Could not start entry point(" + name + ")!", e);
        }
    }
}
