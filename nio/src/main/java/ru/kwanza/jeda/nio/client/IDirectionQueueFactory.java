package ru.kwanza.jeda.nio.client;

import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.api.ISystemManager;
import ru.kwanza.jeda.api.internal.ISystemManagerInternal;

import java.net.InetSocketAddress;

/**
 * @author Guzanov Alexander
 */
public interface IDirectionQueueFactory<E extends ITransportEvent> {
    public IQueue<E> create(ISystemManagerInternal systemManager, InetSocketAddress socketAddress);
}
