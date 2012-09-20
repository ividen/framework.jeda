package ru.kwanza.jeda.nio.client;

/**
 * @author Guzanov Alexander
 */
public interface IConnectErrorHandler<E extends ITransportEvent> {
    void handleConnectError(E event, Throwable e);
}
