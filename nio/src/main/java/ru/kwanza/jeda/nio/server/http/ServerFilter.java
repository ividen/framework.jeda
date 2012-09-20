package ru.kwanza.jeda.nio.server.http;

import ru.kwanza.jeda.nio.utils.HttpUtil;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpPacket;
import org.glassfish.grizzly.http.HttpRequestPacket;

import java.io.IOException;

import static ru.kwanza.jeda.nio.server.http.HttpServer.logger;

/**
 * @author Guzanov Alexander
 */
class ServerFilter extends BaseFilter {
    private EntryPoint entryPoint;
    private HttpServer server;

    public ServerFilter(HttpServer server, EntryPoint entryPoint) {
        this.server = server;
        this.entryPoint = entryPoint;
    }

    @Override
    public NextAction handleRead(FilterChainContext ctx) throws IOException {
        Object message = ctx.getMessage();
        if (!(message instanceof HttpContent)) {
            return ctx.getStopAction();
        }

        final HttpContent httpContent = (HttpContent) ctx.getMessage();
        if (!httpContent.isLast()) {
            return ctx.getStopAction();
        }

        final HttpRequestPacket header = (HttpRequestPacket) httpContent.getHttpHeader();
        String requestURI = header.getRequestURI();
        IHttpHandler handler = server.findHandler(requestURI);
        if (handler != null) {
            return handler.handle(server, entryPoint, httpContent, requestURI, ctx);
        } else {
            if (logger.isWarnEnabled()) {
                logger.warn("HttpServer({},{}) : Handler for uri {} NOT FOUND!",
                        new Object[]{server.getName(), entryPoint.getName()}, requestURI);
            }
            final HttpPacket response = HttpUtil.create404(header);
            ctx.write(response);
            return ctx.getStopAction();
        }
    }
}
