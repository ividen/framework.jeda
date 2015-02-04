package ru.kwanza.jeda.nio.client.http;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.FilterChainEvent;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpResponsePacket;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.ISink;
import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.nio.client.AbstractFilter;
import ru.kwanza.jeda.nio.client.ConnectionContext;
import ru.kwanza.jeda.nio.client.ITransportEvent;
import ru.kwanza.jeda.nio.client.http.exception.ConnectionException;
import ru.kwanza.jeda.nio.client.http.exception.TimeoutException;
import ru.kwanza.jeda.nio.client.timeouthandler.TimeoutHandler;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Michael Yeskov
 */
public class DelegatingHttpClientHandler extends AbstractFilter {
    private static final String ALREADY_SEND_RESPONSE = "ALREADY_SEND_RESPONSE";
    private static final Logger log = LoggerFactory.getLogger(DelegatingHttpClientHandler.class);

    private IJedaManager jedaManager;

    public DelegatingHttpClientHandler(IJedaManager jedaManager) {
        this.jedaManager = jedaManager;
    }

    @Override
    public void handleConnectError(ITransportEvent event, Throwable e) {
        pushResponse(null, ((IDelegatingTransportEvent)event).getResponseStageName(), new HttpResponseEvent(event, null, new ConnectionException(e), null));
    }


    @Override
    public NextAction write(FilterChainContext ctx) throws IOException {
        getConnectionContext(ctx).remove(ALREADY_SEND_RESPONSE);
        return super.write(ctx);
    }

    @Override
    public NextAction read(FilterChainContext ctx) throws IOException {
        IDelegatingTransportEvent requestEvent = (IDelegatingTransportEvent)getConnectionContext(ctx).getRequestEvent();
        HttpContent content = (HttpContent) ctx.getMessage();
        HttpStatus httpStatus = ((HttpResponsePacket) content.getHttpHeader()).getHttpStatus();

        pushResponse(ctx, requestEvent.getResponseStageName(), new HttpResponseEvent(requestEvent, content, null, httpStatus));

        releaseConnection(ctx);

        return ctx.getStopAction();
    }

    public NextAction handleEvent(FilterChainContext ctx, FilterChainEvent event) throws IOException {
        if (event instanceof BufferOverflowEvent){
            IDelegatingTransportEvent requestEvent = (IDelegatingTransportEvent)getConnectionContext(ctx).getRequestEvent();
            pushResponse(ctx, requestEvent.getResponseStageName(), new HttpResponseEvent(requestEvent, null, (BufferOverflowEvent) event, null));

            forceCloseConnection(ctx.getConnection());

            return ctx.getStopAction();
        } else {
            return ctx.getInvokeAction();
        }
    }

    private void pushResponse(FilterChainContext ctx, String stageName, HttpResponseEvent event) {
        ConnectionContext context = null;
        if (ctx != null) {
            context = getConnectionContext(ctx);
            if (context.get(ALREADY_SEND_RESPONSE) != null) {
                log.debug("Already send response for this connection. Skipping event = {}", event);
                return;
            }
        }

        ISink<HttpResponseEvent> responseSink = jedaManager.getStage(stageName).getSink();
        try {
            responseSink.put(Arrays.asList(event));
            if (context != null) {
                context.put(ALREADY_SEND_RESPONSE, ALREADY_SEND_RESPONSE);
            }
        } catch (SinkException e) {
            log.error("Exception while pushing HttpResponseEvent to stage " + stageName + ". Event will be discarded", e);
        }
    }

    @Override
    public NextAction handleClose(final FilterChainContext ctx) throws IOException {
        try {
            TimeoutHandler.checkTimedOut(ctx.getConnection());
        } catch (Exception e) {
            IDelegatingTransportEvent event = (IDelegatingTransportEvent)getConnectionContext(ctx).getRequestEvent();
            pushResponse(ctx, event.getResponseStageName(), new HttpResponseEvent(event, null, new TimeoutException(e), null));
        }

        return ctx.getInvokeAction();
    }

    @Override
    public void exceptionOccurred(FilterChainContext ctx, Throwable error) {
        IDelegatingTransportEvent event = (IDelegatingTransportEvent)getConnectionContext(ctx).getRequestEvent();
        pushResponse(ctx, event.getResponseStageName(), new HttpResponseEvent(event, null, error, null));

        forceCloseConnection(ctx.getConnection());
    }

    private void forceCloseConnection(Connection connection) {
        TimeoutHandler.forget(connection);
        connection.closeSilently();
    }
}
