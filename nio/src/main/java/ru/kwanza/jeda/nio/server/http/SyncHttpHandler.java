package ru.kwanza.jeda.nio.server.http;

import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpPacket;

import static ru.kwanza.jeda.nio.server.http.HttpServer.logger;

/**
 * @author Guzanov Alexander
 */
public abstract class SyncHttpHandler implements IHttpHandler {

    public final NextAction handle(IHttpServer server, IEntryPoint entryPoint,
                                   HttpContent content, String uri, FilterChainContext ctx) {
        boolean debugEnabled = logger.isDebugEnabled();
        boolean traceEnabled = false;
        if (debugEnabled) {
            traceEnabled = logger.isTraceEnabled();
            if (traceEnabled) {
                logger.trace("HttpServer({},{}) : Synchronous request(uri={}) : {}",
                        new Object[]{server.getName(), entryPoint.getName(),
                                uri, TraceUtil.getDescription(content)});
            } else {
                logger.debug("HttpServer({},{}) : Synchronous request(uri={})",
                        new Object[]{server.getName(), entryPoint.getName(), uri});
            }
        }

        HttpPacket response = handle(server, entryPoint, content);
        if (debugEnabled) {
            if (traceEnabled) {
                logger.trace("HttpServer({},{}) : Synchronous response to request(uri={}) : {}",
                        new Object[]{server.getName(), entryPoint.getName(),
                                uri, TraceUtil.getDescription(response)});
            } else {
                logger.trace("HttpServer({},{}) : Synchronous response to request(uri={}) ",
                        new Object[]{server.getName(), entryPoint.getName(),
                                uri});
            }
        }
        ctx.write(response);

        return ctx.getStopAction();
    }

    protected abstract HttpPacket handle(IHttpServer server, IEntryPoint entryPoint, HttpContent content);
}
