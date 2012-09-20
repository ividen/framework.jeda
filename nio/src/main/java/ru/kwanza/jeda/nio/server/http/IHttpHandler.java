package ru.kwanza.jeda.nio.server.http;

import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.http.HttpContent;

/**
 * @author Guzanov Alexander
 */
public interface IHttpHandler {
    public NextAction handle(IHttpServer server, IEntryPoint entryPoint,
                             HttpContent content, String uri, FilterChainContext ctx);
}
