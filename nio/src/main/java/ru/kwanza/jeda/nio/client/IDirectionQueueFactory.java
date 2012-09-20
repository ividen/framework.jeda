package ru.kwanza.jeda.nio.client;

import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.api.internal.ISystemManager;

import java.net.InetSocketAddress;

/**
 * @author Guzanov Alexander
 */
public interface IDirectionQueueFactory<E extends ITransportEvent> {
    public IQueue<E> create(ISystemManager systemManager, InetSocketAddress socketAddress);
}
