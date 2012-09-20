package ru.kwanza.jeda.nio.client;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;

/**
 * @author Guzanov Alexander
 */
public abstract class AbstractFilter<E extends ITransportEvent> extends BaseFilter {
    public final ConnectionContext getConnectionContext(FilterChainContext ctx) {
        ConnectionPool pool = getPool(ctx);
        return pool.getConnectionContext(ctx.getConnection());
    }

    public final void releaseConnection(FilterChainContext ctx) {
        ConnectionPool pool = getPool(ctx);
        pool.releaseConnection(ctx.getConnection(), getConnectionContext(ctx));
    }

    public final void closeConnection(FilterChainContext ctx) {
        ConnectionPool pool = getPool(ctx);
        Connection connection = ctx.getConnection();
        connection.closeSilently();
        pool.releaseConnection(ctx.getConnection(),getConnectionContext(ctx));
    }

    private ConnectionPool getPool(FilterChainContext ctx) {
        return (ConnectionPool) ctx.getInternalContext().getAttributes().getAttribute(ConnectionPool.class.getName());
    }
}
