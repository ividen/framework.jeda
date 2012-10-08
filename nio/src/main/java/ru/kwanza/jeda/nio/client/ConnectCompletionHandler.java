package ru.kwanza.jeda.nio.client;

import org.glassfish.grizzly.CompletionHandler;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.nio.NIOConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.nio.channels.SocketChannel;

/**
 * @author Guzanov Alexander
 */
class ConnectCompletionHandler implements CompletionHandler<Connection> {
    private ITransportEvent event;
    private ConnectionPool connectionPool;
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);

    ConnectCompletionHandler(ConnectionPool connectionPool, ITransportEvent event) {
        this.connectionPool = connectionPool;
        this.event = event;
    }

    public void cancelled() {
        if (logger.isTraceEnabled()) {
            logger.trace("Connection to {} for event {} canceled", event.getConnectionConfig().getEndpoint(), event);
        }
        connectionPool.returnConnection(null, event.getConnectionConfig());
    }

    public void failed(Throwable throwable) {
        logger.error("Error connect to " + event.getConnectionConfig().getEndpoint() +
                " for event " + event + " canceled", throwable);
        connectionPool.returnConnection(null, event.getConnectionConfig());
        IConnectErrorHandler connectErrorHandler = event.getConnectErrorHandler();
        if (connectErrorHandler != null) {
            connectErrorHandler.handleConnectError(event, throwable);
        }
    }

    public void completed(Connection result) {
        if (result != null) {
            connectionPool.registerConnection(result, event);
            result.addCloseListener(new ConnectionCloseListener(connectionPool, event.getConnectionConfig()));
            configConnection(result);
            result.setProcessor(event.getProcessingFilterChain());

            result.write(event.getContent());
        }
    }

    private void configConnection(Connection connection) {
        //todo aguzanov конфигурирование сокета должно осуществляться в момент установки соединения
        if (connection instanceof NIOConnection) {
            NIOConnection nioConnection = (NIOConnection) connection;
            if (nioConnection.getChannel() instanceof SocketChannel) {
                SocketChannel channel = (SocketChannel) nioConnection.getChannel();

                Socket socket = channel.socket();
                ConnectionConfig config = event.getConnectionConfig();
                Integer soTimeout = config.getSoTimeout();

                if (soTimeout != null) {
                    try {
                        socket.setSoTimeout(soTimeout);
                    } catch (Throwable e) {
                        logger.warn("Can't set soTimeout for event " + event, e);
                    }
                }

                Boolean keepAlive = config.isKeepAlive();
                if (keepAlive != null) {
                    try {
                        socket.setKeepAlive(keepAlive);
                    } catch (Exception e) {
                        logger.warn("Can't set keepAlive for event " + event, e);
                    }
                }

                Boolean tcpNoDelay = config.isTcpNoDelay();
                if (tcpNoDelay != null) {
                    try {
                        socket.setTcpNoDelay(tcpNoDelay);
                    } catch (Exception e) {
                        logger.warn("Can't set tcpNoDelay for event " + event, e);
                    }
                }

            }
        }
    }

    public void updated(Connection result) {
    }
}
