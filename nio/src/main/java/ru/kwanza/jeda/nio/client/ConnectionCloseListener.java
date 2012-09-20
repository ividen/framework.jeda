package ru.kwanza.jeda.nio.client;

import org.glassfish.grizzly.Connection;

import java.io.IOException;

/**
 * @author Guzanov Alexander
 */
class ConnectionCloseListener implements Connection.CloseListener {
    private ConnectionPool pool;
    private ConnectionConfig config;

    public ConnectionCloseListener(ConnectionPool pool, ConnectionConfig config) {
        this.pool = pool;
        this.config = config;
    }

    public void onClosed(Connection connection, Connection.CloseType type) throws IOException {
        pool.returnConnection(connection, config, true);
    }
}
