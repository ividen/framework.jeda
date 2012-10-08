package ru.kwanza.jeda.nio.client;

import org.glassfish.grizzly.Connection;

/**
 * @author Guzanov Alexander
 */
final class ConnectionHolder {
    private ConnectionConfig connectionConfig;
    private Connection connection;
    private long expiryTimestamp;
    private boolean useRequestCount = false;
    private long requestCount = -1;

    public ConnectionHolder(Connection connection, ConnectionConfig connectionConfig, long maxRequestCount) {
        this.connection = connection;
        this.connectionConfig = connectionConfig;
        this.expiryTimestamp = System.currentTimeMillis();
        if (maxRequestCount > 0) {
            this.useRequestCount = true;
            this.requestCount = maxRequestCount;
        }
    }

    public ConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    public Connection getConnection() {
        return connection;
    }

    public long getExpiryTimestamp() {
        return expiryTimestamp;
    }

    public void update(long keepAliveTimeout) {
        this.expiryTimestamp = System.currentTimeMillis() + keepAliveTimeout;
        if (useRequestCount) {
            this.requestCount--;
        }

    }

    public boolean isTimedOut() {
        if (expiryTimestamp < System.currentTimeMillis()) {
            return true;
        }

        if (useRequestCount && requestCount <= 0) {
            return true;
        }

        return false;
    }
}
