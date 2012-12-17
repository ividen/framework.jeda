package ru.kwanza.jeda.nio.server.http;

import liquibase.util.StreamUtil;
import org.glassfish.grizzly.http.HttpRequestPacket;
import ru.kwanza.jeda.api.IFlowBus;
import ru.kwanza.jeda.api.ISink;
import ru.kwanza.jeda.api.IStage;
import ru.kwanza.jeda.api.internal.ISystemManager;
import ru.kwanza.jeda.nio.utils.HttpUtil;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Alexander Guzanov
 */
public class JedaWSHttpHandler extends JedaHttpHandler {
    private String wsdl;
    private AtomicReference<byte[]> wsdlContent = new AtomicReference<byte[]>();

    public static JedaWSHttpHandler createForFlowBus(ISystemManager manager, IFlowBus flowBus, String wsdlUrl) {
        return createForFlowBus(manager, flowBus, Const.DEFAULT_FLEX_FLOW_RESPONSE_TIMEOUT, wsdlUrl);
    }

    public static JedaWSHttpHandler createForFlowBus(ISystemManager manager, IFlowBus flowBus,
                                                     long timeout, String wsdlUrl) {
        return new JedaWSHttpHandler(manager, flowBus, timeout, wsdlUrl);
    }

    public static JedaWSHttpHandler createForStage(ISystemManager manager, IStage stage,
                                                   String wsdlUrl) {
        return createForStage(manager, stage, Const.DEFAULT_FLEX_FLOW_RESPONSE_TIMEOUT, wsdlUrl);
    }

    public static JedaWSHttpHandler createForStage(ISystemManager manager, IStage stage,
                                                   long timeout, String wsdlUrl) {
        return new JedaWSHttpHandler(manager, stage.<IHttpEvent>getSink(), timeout, wsdlUrl);
    }

    public static JedaWSHttpHandler createForObject(ISystemManager manager, String objectName,
                                                    String wsdlUrl) {
        return createForObject(manager, objectName, Const.DEFAULT_FLEX_FLOW_RESPONSE_TIMEOUT, wsdlUrl);
    }

    public static JedaWSHttpHandler createForObject(ISystemManager manager, String objectName,
                                                    long timeout, String wsdlUrl) {
        Object obj = manager.resolveObject(objectName);
        ISink sink = obj instanceof IStage ? ((IStage) obj).getSink() : (ISink) obj;
        return new JedaWSHttpHandler(manager, sink, timeout, wsdlUrl);
    }

    public JedaWSHttpHandler(ISystemManager manager, ISink<IHttpEvent> sink, long timeout, String wsdl) {
        super(manager, sink, timeout);
        this.wsdl = wsdl;
    }

    @Override
    protected void handle(IHttpRequest request) {
        if (request.getParameter("wsdl") == null) {
            super.handle(request);

        } else {
            byte[] content = wsdlContent.get();
            if (content == null) {
                try {
                    InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(wsdl);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    StreamUtil.copy(inputStream, baos);
                    content = baos.toByteArray();
                    wsdlContent.set(content);
                } catch (Exception e) {
                    HttpServer.logger.error("Error reading wsdl!", e);
                    request.finish(HttpUtil.create500((HttpRequestPacket) request.getContent().getHttpHeader(), e));
                    return;
                }
            }
            request.finish(HttpUtil.createResponse(request, "text/xml", content));
        }


    }
}
