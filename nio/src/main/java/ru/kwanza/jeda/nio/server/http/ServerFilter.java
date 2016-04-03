package ru.kwanza.jeda.nio.server.http;

import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpPacket;
import org.glassfish.grizzly.http.HttpRequestPacket;
import ru.kwanza.jeda.nio.utils.HttpUtil;

import java.io.IOException;

import static ru.kwanza.jeda.nio.server.http.HttpServer.logger;

/**
 * @author Guzanov Alexander
 */
class ServerFilter extends BaseFilter {
    private EntryPoint entryPoint;
    private HttpServer server;

    private static Attribute<HttpContent> CONTENT = Grizzly.DEFAULT_ATTRIBUTE_BUILDER.createAttribute(ServerFilter.class.getSimpleName() + ".chunk");


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

        final HttpContent chunkContent = ctx.getMessage();

        HttpContent requestContent = CONTENT.get(ctx.getConnection());
        if (!chunkContent.isLast()) {

            if (requestContent != null) {
                requestContent.append(chunkContent);
            } else {
                requestContent = HttpContent.builder(chunkContent.getHttpHeader()).content(chunkContent.getContent()).build();
                CONTENT.set(ctx.getConnection(), requestContent);
            }

            return ctx.getStopAction();
        } else if (requestContent != null) {
            requestContent.append(chunkContent);
            requestContent.setLast(true);
        } else {
            requestContent = chunkContent;
        }


        CONTENT.remove(ctx.getConnection());

        final HttpRequestPacket header = (HttpRequestPacket) requestContent.getHttpHeader();
        String requestURI = header.getRequestURI();
        IHttpHandler handler = server.findHandler(requestURI);
        if (handler != null) {
            return handler.handle(server, entryPoint, requestContent, requestURI, ctx);
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

    @Override
    public NextAction handleClose(FilterChainContext ctx) throws IOException {
        CONTENT.remove(ctx.getConnection());
        return ctx.getInvokeAction();
    }

    @Override
    public void exceptionOccurred(FilterChainContext ctx, Throwable error) {
        CONTENT.remove(ctx.getConnection());
        super.exceptionOccurred(ctx, error);
    }
}
