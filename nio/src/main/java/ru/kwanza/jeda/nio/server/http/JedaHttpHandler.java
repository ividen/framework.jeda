package ru.kwanza.jeda.nio.server.http;

import org.glassfish.grizzly.http.HttpRequestPacket;
import ru.kwanza.jeda.api.ISink;
import ru.kwanza.jeda.api.IStage;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.nio.utils.HttpUtil;

import java.util.Collections;

/**
 * @author Guzanov Alexander
 */
public class JedaHttpHandler extends AsyncHttpHandler {
    private ISink<IHttpEvent> sink;
    private IJedaManager manager;

    public static JedaHttpHandler createForObjectRef(IJedaManager manager, ISink object, long timeout) {
        return new JedaHttpHandler(manager, object, timeout);
    }

    public static JedaHttpHandler createForObject(IJedaManager manager, String objectName, long timeout) {
        Object obj = manager.resolveObject(objectName);
        ISink sink = obj instanceof IStage ? ((IStage) obj).getSink() : (ISink) obj;
        return createForObjectRef(manager, sink, timeout);
    }

    public JedaHttpHandler(IJedaManager manager, ISink<IHttpEvent> sink, long timeout) {
        super(timeout);
        this.manager = manager;
        this.sink = sink;
    }

    @Override
    protected void handle(IHttpRequest request) {
        try {
            sink.put(Collections.<IHttpEvent>singletonList(new HttpEventImpl(request)));
        } catch (Throwable e) {
            request.finish(HttpUtil.create500((HttpRequestPacket) request.getContent().getHttpHeader(), e));
        }
    }
}
