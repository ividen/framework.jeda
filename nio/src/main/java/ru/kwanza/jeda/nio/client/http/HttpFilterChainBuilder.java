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
@Component
public class HttpFilterChainBuilder {
    @Resource
    private IJedaManager jedaManager;

    private long defMaxBufferCapacity = 10000000;
    private int defChunkedMaxHeaderSize = 10000000;
    private FilterChain httpDefaultFixedSize;
    private FilterChain httpDefaultChunked;

    @PostConstruct
    public void init() {
        httpDefaultChunked = buildHttp(new ChunkedTransferEncoding(defChunkedMaxHeaderSize), defMaxBufferCapacity);
        httpDefaultFixedSize = buildHttp(new FixedLengthTransferEncoding(), defMaxBufferCapacity);
    }


    public FilterChain getHttpDefaultFixedSize() {
        return httpDefaultFixedSize;
    }

    public FilterChain getHttpDefaultChunked() {
        return httpDefaultChunked;
    }


    public FilterChain buildCustom(TransferEncoding transferEncoding, SSLEngineConfigurator sslEngineConfigurator, long customMaxBufferCapacity) {
        FilterChainBuilder builder = FilterChainBuilder.stateless();

        builder.add(new TransportFilter());

        if (sslEngineConfigurator != null) {
            builder.add(new SSLFilter(null, sslEngineConfigurator));
        }

        HttpClientFilter clientFilter = new HttpClientFilter();
        clientFilter.addTransferEncoding(transferEncoding);
        builder.add(clientFilter);

        builder.add(new AccumulatingHttpClientFilter(customMaxBufferCapacity));
        builder.add(new DelegatingHttpClientHandler(jedaManager));

        return builder.build();
    }

    public FilterChain buildHttps(TransferEncoding transferEncoding, SSLEngineConfigurator sslEngineConfigurator, long customMaxBufferCapacity) {
        return buildCustom(transferEncoding, sslEngineConfigurator, customMaxBufferCapacity);
    }

    public FilterChain buildHttp(TransferEncoding transferEncoding, long customMaxBufferCapacity) {
        return buildCustom(transferEncoding, null, customMaxBufferCapacity);
    }


    public long getDefMaxBufferCapacity() {
        return defMaxBufferCapacity;
    }

    public void setDefMaxBufferCapacity(long defMaxBufferCapacity) {
        this.defMaxBufferCapacity = defMaxBufferCapacity;
    }

    public int getDefChunkedMaxHeaderSize() {
        return defChunkedMaxHeaderSize;
    }

    public void setDefChunkedMaxHeaderSize(int defChunkedMaxHeaderSize) {
        this.defChunkedMaxHeaderSize = defChunkedMaxHeaderSize;
    }
}
