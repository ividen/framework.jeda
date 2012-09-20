package ru.kwanza.jeda.nio.client;

import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.api.internal.ISystemManager;
import ru.kwanza.jeda.core.queue.TransactionalMemoryQueue;

import java.net.InetSocketAddress;

/**
 * @author Guzanov Alexander
 */
class DefaultDirectionQueueFactory<E extends ITransportEvent> implements IDirectionQueueFactory<E> {
    public IQueue<E> create(ISystemManager systemManager, InetSocketAddress socketAddress) {
        return new TransactionalMemoryQueue<E>(systemManager);
    }
}
