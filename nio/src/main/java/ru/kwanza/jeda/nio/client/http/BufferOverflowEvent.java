package ru.kwanza.jeda.nio.client.http;

import org.glassfish.grizzly.filterchain.FilterChainEvent;

/**
 * @author Michael Yeskov
 */
public class BufferOverflowEvent extends Throwable implements FilterChainEvent  {
    private static final String TYPE = "BUFFER_OVERFLOW_EVENT";

    private long maxBufferCapacity;

    public BufferOverflowEvent(long maxBufferCapacity) {
        this.maxBufferCapacity = maxBufferCapacity;
    }

    public String getMessage() {
        return "Read buffer exceeded maximum capacity " + maxBufferCapacity;
    }

    @Override
    public Object type() {
        return TYPE;
    }
}
