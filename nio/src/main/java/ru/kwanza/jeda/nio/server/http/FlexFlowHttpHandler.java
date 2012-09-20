package ru.kwanza.jeda.nio.server.http;

import ru.kwanza.jeda.api.IFlowBus;
import ru.kwanza.jeda.api.ISink;
import ru.kwanza.jeda.api.IStage;
import ru.kwanza.jeda.api.internal.ISystemManager;
import ru.kwanza.jeda.nio.utils.HttpUtil;
import org.glassfish.grizzly.http.HttpRequestPacket;

import java.util.Collections;

/**
 * @author Guzanov Alexander
 */
public final class FlexFlowHttpHandler extends AsyncHttpHandler {
    private ISink<IHttpEvent> sink;
    private ISystemManager manager;

    public static FlexFlowHttpHandler createForFlowBus(ISystemManager manager, IFlowBus flowBus) {
        return createForFlowBus(manager, flowBus, Const.DEFAULT_FLEX_FLOW_RESPONSE_TIMEOUT);
    }

    public static FlexFlowHttpHandler createForFlowBus(ISystemManager manager, IFlowBus flowBus, long timeout) {
        return new FlexFlowHttpHandler(manager, flowBus, timeout);
    }

    public static FlexFlowHttpHandler createForStage(ISystemManager manager, IStage stage) {
        return createForStage(manager, stage, Const.DEFAULT_FLEX_FLOW_RESPONSE_TIMEOUT);
    }

    public static FlexFlowHttpHandler createForStage(ISystemManager manager, IStage stage, long timeout) {
        return new FlexFlowHttpHandler(manager, stage.<IHttpEvent>getSink(), timeout);
    }

    public static FlexFlowHttpHandler createForObject(ISystemManager manager, String objectName) {
        return createForObject(manager, objectName, Const.DEFAULT_FLEX_FLOW_RESPONSE_TIMEOUT);
    }

    public static FlexFlowHttpHandler createForObject(ISystemManager manager, String objectName, long timeout) {
        Object obj = manager.resolveObject(objectName);
        ISink sink = obj instanceof IStage ? ((IStage) obj).getSink() : (ISink) obj;
        return new FlexFlowHttpHandler(manager, sink, timeout);
    }

    public FlexFlowHttpHandler(ISystemManager manager, ISink<IHttpEvent> sink, long timeout) {
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
