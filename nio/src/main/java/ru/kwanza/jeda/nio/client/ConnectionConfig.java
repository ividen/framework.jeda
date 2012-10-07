package ru.kwanza.jeda.nio.client;

import java.net.InetSocketAddress;

/**
 * @author Guzanov Alexander
 */
public class ConnectionConfig {
    private boolean keepAlive;
    private Boolean tcpNoDelay;
    //todo aguzanov Возможность работы клиентских соединений через proxy
    private boolean useProxy;

    private InetSocketAddress socketAddress;
    private Integer readTimeout;

    public ConnectionConfig(InetSocketAddress socketAddress) {
        this(socketAddress, false, null, null);
    }

    public ConnectionConfig(InetSocketAddress socketAddress, boolean keepAlive, Boolean tcpNoDelay, Integer readTimeout) {
        this.socketAddress = socketAddress;
        this.keepAlive = keepAlive;
        this.tcpNoDelay = tcpNoDelay;
        this.readTimeout = readTimeout;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public InetSocketAddress getEndpoint() {
        return socketAddress;
    }

    public Integer getSoTimeout() {
        return readTimeout;
    }
}
