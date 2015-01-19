package ru.kwanza.jeda.nio.client.soap;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.filterchain.FilterChain;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.Protocol;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.memory.Buffers;
import ru.kwanza.jeda.nio.client.ConnectionConfig;
import ru.kwanza.jeda.nio.client.http.IDelegatingTransportEvent;
import ru.kwanza.toolbox.attribute.AttributeHolder;

import java.nio.charset.Charset;

/**
 * @author Michael Yeskov
 */
public class SOAPTransportEvent implements IDelegatingTransportEvent {

    protected String message;
    protected String URI;
    protected ConnectionConfig connectionConfig;
    protected String responseStageName;
    protected FilterChain filterChain;
    protected SOAPVersion soapVersion;
    protected String soapAction;



    public SOAPTransportEvent(String message, ConnectionConfig connectionConfig, String URI, String responseStageName, FilterChain filterChain) {
        this.message = message;
        this.URI = URI;
        this.connectionConfig = connectionConfig;
        this.responseStageName = responseStageName;
        this.filterChain = filterChain;
    }

    public SOAPTransportEvent(String message, ConnectionConfig connectionConfig, String URI, String responseStageName, FilterChain filterChain, SOAPVersion soapVersion, String soapAction) {
        this(message, connectionConfig, URI, responseStageName, filterChain);
        this.soapVersion = soapVersion;
        this.soapAction = soapAction;
    }

    @Override
    public String getResponseStageName() {
        return responseStageName;
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

        final Buffer wrap = Buffers.wrap(null, message.getBytes(Charset.forName("UTF-8")));

        StringBuilder contentType = new StringBuilder("application/soap+xml;charset=UTF-8");
        StringBuilder actionBuilder = null;

        if (soapAction != null) {
            if (soapVersion == SOAPVersion.SOAP1_2) {
                contentType.append(";action=\"");
                contentType.append(soapAction);
                contentType.append("\"");
            } else if (soapVersion == SOAPVersion.SOAP1_1)  {
                actionBuilder = new StringBuilder("\"");
                actionBuilder.append(soapAction).append("\"");
            }
        }

        final HttpRequestPacket.Builder requestBuilder = HttpRequestPacket.builder();
        requestBuilder.method("POST").uri(URI).protocol(Protocol.HTTP_1_1).chunked(false).contentLength(wrap.capacity())
                .header(Header.Host, connectionConfig.getEndpoint().getHostName() + ":" + connectionConfig.getEndpoint().getPort())
                .contentType(contentType.toString());

        if (actionBuilder != null) {
            requestBuilder.header("SOAPAction", actionBuilder.toString());
        }

        if (connectionConfig.isKeepAlive()) {
            requestBuilder.header(Header.Connection, "keep-alive");
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
