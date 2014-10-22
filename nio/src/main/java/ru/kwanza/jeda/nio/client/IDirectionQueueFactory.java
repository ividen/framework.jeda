package ru.kwanza.jeda.nio.client;

import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.internal.IQueue;

import java.net.InetSocketAddress;

/**
 * @author Guzanov Alexander
 */
public interface IDirectionQueueFactory<E extends ITransportEvent> {
    public IQueue<E> create(IJedaManager systemManager, InetSocketAddress socketAddress);
}
