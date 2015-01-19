package ru.kwanza.jeda.nio.client.http;

import org.glassfish.grizzly.filterchain.FilterChain;
import ru.kwanza.jeda.nio.client.ConnectionConfig;
import ru.kwanza.jeda.nio.client.soap.SOAPTransportEvent;

import java.net.InetSocketAddress;

/**
 * @author Michael Yeskov
 */
public class ApplicationSoapEvent extends SOAPTransportEvent{

    public ApplicationSoapEvent(String message, FilterChain filterChain) {
        super(message, null, "/ws_test-1/ws/soap", "responseStage", filterChain);
        connectionConfig = new ConnectionConfig(new InetSocketAddress("localhost", 8080),
                true, true, 1000);
    }
}
