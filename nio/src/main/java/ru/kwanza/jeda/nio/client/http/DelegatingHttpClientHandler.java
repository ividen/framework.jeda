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
import ru.kwanza.jeda.nio.client.http.exception.UnexpectedConnectionTermination;
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
        ConnectionContext context = getConnectionContext(ctx);
        IDelegatingTransportEvent requestEvent = (IDelegatingTransportEvent)context.getRequestEvent();
        HttpContent content = (HttpContent) ctx.getMessage();
        HttpStatus httpStatus = ((HttpResponsePacket) content.getHttpHeader()).getHttpStatus();

        pushResponse(context, requestEvent.getResponseStageName(), new HttpResponseEvent(requestEvent, content, null, httpStatus));

        releaseConnection(ctx); //release должен быть после push response иначе мы пошлем сообщение о том, что connection был прерван вместо ответа

        return ctx.getStopAction();
    }

    public NextAction handleEvent(FilterChainContext ctx, FilterChainEvent event) throws IOException {
        if (event instanceof BufferOverflowEvent){
            pushExceptionResponse(ctx, (BufferOverflowEvent)event);

            forceCloseConnection(ctx.getConnection());

            return ctx.getStopAction();
        } else {
            return ctx.getInvokeAction();
        }
    }

    @Override
    public NextAction handleClose(final FilterChainContext ctx) throws IOException {
        try {
            TimeoutHandler.checkTimedOut(ctx.getConnection());
        } catch (Exception e) {
            pushExceptionSkipDuplicates(ctx, new TimeoutException(e));
        }
        pushExceptionSkipDuplicates(ctx, new UnexpectedConnectionTermination()); //send exception on unexpected connection termination

        return ctx.getInvokeAction();
    }

    @Override
    public void exceptionOccurred(FilterChainContext ctx, Throwable error) {
        pushExceptionResponse(ctx, error);
        forceCloseConnection(ctx.getConnection());
    }

    private void pushExceptionSkipDuplicates(FilterChainContext ctx, Throwable error) {
        if (getConnectionContext(ctx).get(ALREADY_SEND_RESPONSE) == null) { //silently skip duplicates because they can normally happens
            pushExceptionResponse(ctx, error);
        }
    }

    private void pushExceptionResponse(FilterChainContext ctx, Throwable error) {
        ConnectionContext context = getConnectionContext(ctx);
        IDelegatingTransportEvent requestEvent = (IDelegatingTransportEvent)context.getRequestEvent();
        if (requestEvent != null) { //can be null when keep alive connection is closed by timeout
            pushResponse(context, requestEvent.getResponseStageName(), new HttpResponseEvent(requestEvent, null, error, null));
        }
    }

    private void pushResponse(ConnectionContext context, String stageName, HttpResponseEvent event) {
        if (context != null) { //additional check for unexpected double response
            if (context.get(ALREADY_SEND_RESPONSE) != null) {
                log.warn("Already send response for this connection. Skipping event = {}", event);
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

    private void forceCloseConnection(Connection connection) {
        TimeoutHandler.forget(connection);
        connection.closeSilently();
    }
}
