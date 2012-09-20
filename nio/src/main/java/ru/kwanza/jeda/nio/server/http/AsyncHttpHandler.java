package ru.kwanza.jeda.nio.server.http;

import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.http.HttpContent;

import static ru.kwanza.jeda.nio.server.http.HttpServer.logger;

/**
 * @author Guzanov Alexander
 */
public abstract class AsyncHttpHandler implements IHttpHandler {
    protected final long timeout;
    private ITimedOutHandler timedOutHandler;

    public AsyncHttpHandler(long timeout) {
        this.timeout = timeout;
    }

    protected abstract void handle(IHttpRequest request);

    public final NextAction handle(IHttpServer server, IEntryPoint entryPoint,
                                   HttpContent content, String uri, FilterChainContext ctx) {
        HttpServer httpServer = (HttpServer) server;
        RequestImpl asyncRequest = new RequestImpl(uri, ctx, httpServer,
                (EntryPoint) entryPoint, content, timedOutHandler, timeout);

        NextAction suspendAction = ctx.getSuspendAction();
        httpServer.suspend(asyncRequest);


        if (logger.isDebugEnabled()) {
            if (logger.isTraceEnabled()) {
                logger.trace("HttpServer({},{}) : Creating Asynchronous request(id={},uri={},ts={}) : {}",
                        new Object[]{server.getName(), entryPoint.getName(),
                                asyncRequest.getID(), asyncRequest.getUri(), asyncRequest.getTimestamp(),
                                TraceUtil.getDescription(content)});
            } else {
                logger.debug("HttpServer({},{}) : Creating Asynchronous request(id={},uri={},ts={})",
                        new Object[]{server.getName(), entryPoint.getName(),
                                asyncRequest.getID(), asyncRequest.getUri(), asyncRequest.getTimestamp()});
            }
        }

        handle(asyncRequest);

        return suspendAction;
    }

    public ITimedOutHandler getTimedOutHandler() {
        return timedOutHandler;
    }

    public void setTimedOutHandler(ITimedOutHandler timedOutHandler) {
        this.timedOutHandler = timedOutHandler;
    }
}
