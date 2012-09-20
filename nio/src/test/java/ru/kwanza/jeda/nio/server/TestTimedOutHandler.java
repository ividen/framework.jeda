package ru.kwanza.jeda.nio.server;

import ru.kwanza.jeda.nio.server.http.IHttpRequest;
import ru.kwanza.jeda.nio.server.http.ITimedOutHandler;
import ru.kwanza.jeda.nio.utils.HttpUtil;
import org.glassfish.grizzly.http.HttpRequestPacket;

/**
 * @author Guzanov Alexander
 */
public class TestTimedOutHandler implements ITimedOutHandler {
    public void onTimedOut(IHttpRequest request) {
        request.finish(HttpUtil.createResponse((HttpRequestPacket) request.getContent().getHttpHeader(),
                200, "", "application+txt", "TIMED_OUT!"));
    }
}
