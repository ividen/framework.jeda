package ru.kwanza.jeda.nio.client;

import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import ru.kwanza.jeda.api.IEventProcessor;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
class EventProcessor implements IEventProcessor<ITransportEvent> {
    private TCPNIOTransport nioTransport;
    private ConnectionPool pool;

    public EventProcessor(TCPNIOTransport nioTransport, ConnectionPool pool) {
        this.nioTransport = nioTransport;
        this.pool = pool;
    }

    public void process(Collection<ITransportEvent> events) {
        if (events != null) {
            for (ITransportEvent event : events) {
                pool.write(nioTransport, event);
            }
        }
    }
}
