package ru.kwanza.jeda.nio.client;

import org.glassfish.grizzly.filterchain.FilterChain;
import ru.kwanza.jeda.api.IEvent;

/**
 * @author Guzanov Alexander
 */
public interface ITransportEvent extends IEvent {

    public ConnectionConfig getConnectionConfig();

    //todo aguzanov нужно сделать дефалтную реализацию Http(s)FilterChain, чтобы этим удобно было пользоваться, не не нужно было эту "цепочку" создавать для каждого события
    public FilterChain getProcessingFilterChain();

    public Object getContent();

    //todo aguzanov очень неудобно делать отдельный обработчик событий. Нужно Засунуть это в FilterChain
    public IConnectErrorHandler getConnectErrorHandler();
}
