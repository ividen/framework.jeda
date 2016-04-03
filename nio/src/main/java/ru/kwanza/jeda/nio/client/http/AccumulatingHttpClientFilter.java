package ru.kwanza.jeda.nio.client.http;

import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.http.HttpContent;

import java.io.IOException;

/**
 * @author Michael Yeskov
 */
public class AccumulatingHttpClientFilter extends BaseFilter {
    private long maxBufferCapacity = 10000000;

    public AccumulatingHttpClientFilter() {
    }

    public AccumulatingHttpClientFilter(long maxBufferCapacity) {
        this.maxBufferCapacity = maxBufferCapacity;
    }

    @Override
    public NextAction handleRead(FilterChainContext ctx) throws IOException {
        final HttpContent content = ctx.getMessage();
        if (content.isLast()) {
            return ctx.getInvokeAction();
        } else {
            if (content.getContent().capacity() > maxBufferCapacity) {
                ctx.notifyUpstream(new BufferOverflowEvent(maxBufferCapacity)); //upstream filter must close connection properly
                return ctx.getStopAction();
            } else {
                return ctx.getStopAction(content);
            }
        }
    }
}
