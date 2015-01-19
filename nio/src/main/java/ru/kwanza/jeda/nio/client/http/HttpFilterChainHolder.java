package ru.kwanza.jeda.nio.client.http;

import org.glassfish.grizzly.filterchain.FilterChain;
import org.glassfish.grizzly.http.ChunkedTransferEncoding;
import org.glassfish.grizzly.http.FixedLengthTransferEncoding;
import org.springframework.stereotype.Component;
import ru.kwanza.jeda.api.IJedaManager;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author Michael Yeskov
 */
@Component
public class HttpFilterChainHolder {

    @Resource
    private IJedaManager jedaManager;

    private long maxBufferCapacity = 10000000;
    private FilterChain httpFilterChain;

    @PostConstruct
    public void init(){
        httpFilterChain = HttpFilterChainBuilder.buildHttp(jedaManager, maxBufferCapacity);
    }

    public FilterChain getHttpFilterChain() {
        return httpFilterChain;
    }

    public long getMaxBufferCapacity() {
        return maxBufferCapacity;
    }

    public void setMaxBufferCapacity(long maxBufferCapacity) {
        this.maxBufferCapacity = maxBufferCapacity;
    }
}
