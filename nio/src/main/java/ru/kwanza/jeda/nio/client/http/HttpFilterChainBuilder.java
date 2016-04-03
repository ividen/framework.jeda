package ru.kwanza.jeda.nio.client.http;

import org.glassfish.grizzly.filterchain.FilterChain;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.http.ChunkedTransferEncoding;
import org.glassfish.grizzly.http.FixedLengthTransferEncoding;
import org.glassfish.grizzly.http.HttpClientFilter;
import org.glassfish.grizzly.http.TransferEncoding;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.grizzly.ssl.SSLFilter;
import org.springframework.stereotype.Component;
import ru.kwanza.jeda.api.IJedaManager;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author Michael Yeskov
 */

public class HttpFilterChainBuilder {

    public static FilterChain buildCustom(IJedaManager jedaManager, SSLEngineConfigurator sslEngineConfigurator, long customMaxBufferCapacity) {
        FilterChainBuilder builder = FilterChainBuilder.stateless();

        builder.add(new TransportFilter());

        if (sslEngineConfigurator != null) {
            builder.add(new SSLFilter(null, sslEngineConfigurator));
        }

        builder.add(new HttpClientFilter());

        builder.add(new AccumulatingHttpClientFilter(customMaxBufferCapacity));
        builder.add(new DelegatingHttpClientHandler(jedaManager));

        return builder.build();
    }

    public static FilterChain buildHttps(IJedaManager jedaManager, SSLEngineConfigurator sslEngineConfigurator, long customMaxBufferCapacity) {
        return buildCustom(jedaManager, sslEngineConfigurator, customMaxBufferCapacity);
    }

    public static FilterChain buildHttp(IJedaManager jedaManager, long customMaxBufferCapacity) {
        return buildCustom(jedaManager, null, customMaxBufferCapacity);
    }

}
