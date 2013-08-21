package ru.kwanza.jeda.nio.client;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import ru.kwanza.jeda.nio.client.timeouthandler.TimeoutHandler;

import java.io.IOException;

/**
 * @author Guzanov Alexander
 */
public abstract class AbstractFilter<E extends ITransportEvent> extends BaseFilter {


    public final ConnectionContext getConnectionContext(FilterChainContext ctx) {
        return ConnectionContext.getContext(ctx.getConnection());
    }

    public final void releaseConnection(FilterChainContext ctx) {
        ConnectionPool pool = getPool(ctx);
        pool.releaseConnection(ctx.getConnection());
    }

    public final void closeConnection(FilterChainContext ctx) {
        Connection connection = ctx.getConnection();
        connection.closeSilently();
    }

    public abstract void handleConnectError(E event, Throwable e);

    private ConnectionPool getPool(FilterChainContext ctx) {
        return ConnectionPool.getPool(ctx.getConnection());
    }

    @Override
    public NextAction handleRead(final FilterChainContext ctx) throws IOException {
        final Connection connection = ctx.getConnection();

        TimeoutHandler.registerRead(connection);

        return ctx.getInvokeAction();
    }

    @Override
    public NextAction handleWrite(final FilterChainContext ctx) throws IOException {
        final long ts = getConnectionContext(ctx).getRequestEvent().getConnectionConfig().getSoTimeout();
        final Connection connection = ctx.getConnection();

        TimeoutHandler.registerWrite(ts, connection);

        return ctx.getInvokeAction();
    }

    @Override
    public NextAction handleClose(final FilterChainContext ctx) throws IOException {
        try {
            TimeoutHandler.checkTimedOut(ctx.getConnection());
        } catch (Exception e) {
            exceptionOccurred(ctx, e);
        }

        return ctx.getInvokeAction();
    }

}
