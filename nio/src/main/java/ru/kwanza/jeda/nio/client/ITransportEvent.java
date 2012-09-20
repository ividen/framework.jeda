package ru.kwanza.jeda.nio.client;

import ru.kwanza.jeda.api.IEvent;
import org.glassfish.grizzly.filterchain.FilterChain;

/**
 * @author Guzanov Alexander
 */
public interface ITransportEvent extends IEvent {

    public ConnectionConfig getConnectionConfig();

    public FilterChain getProcessingFilterChain();

    public Object getContent();

    public IConnectErrorHandler getConnectErrorHandler();
}
