package ru.kwanza.jeda.nio.client.http;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.filterchain.Filter;
import org.glassfish.grizzly.filterchain.FilterChain;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.Protocol;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.memory.Buffers;
import ru.kwanza.jeda.nio.client.ConnectionConfig;
import ru.kwanza.toolbox.attribute.AttributeHolder;

import java.net.InetSocketAddress;

/**
 * @author Michael Yeskov
 */
public class HttpRequestEvent implements IDelegatingTransportEvent {
    private static final String URI = "/ws_test-1/ws/soap";

    private FilterChain filterChain;
    private ConnectionConfig connectionConfig;
    private String body;

    public HttpRequestEvent(FilterChain filterChain, String body) {
        this.filterChain = filterChain;
        connectionConfig = new ConnectionConfig(new InetSocketAddress("localhost", 8080),
                true, true, 1000);
        this.body = body;
    }

    @Override
    public String getResponseStageName() {
        return "responseStage";
    }

    @Override
    public ConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    @Override
    public FilterChain getProcessingFilterChain() {
        return filterChain;
    }

    @Override
    public Object getContent() {
        final Buffer wrap = Buffers.wrap(null, body);

        final HttpRequestPacket.Builder requestBuilder = HttpRequestPacket.builder();
        requestBuilder.method("POST").uri(URI).protocol(Protocol.HTTP_1_1).chunked(false).contentLength(wrap.capacity())
                .header(Header.Host, connectionConfig.getEndpoint().getHostName() + ":" + connectionConfig.getEndpoint().getPort())
                .contentType("application/soap+xml");
        if (connectionConfig.isKeepAlive()) {
            requestBuilder.header(Header.Connection, "Keep-Alive");
        } else {
            requestBuilder.header(Header.Connection, "close");
        }
        return HttpContent.builder(requestBuilder.build()).content(wrap).last(true).build();
    }

    @Override
    public AttributeHolder getAttributes() {
        return null;
    }
}
