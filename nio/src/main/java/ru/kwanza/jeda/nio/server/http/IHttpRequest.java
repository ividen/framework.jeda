package ru.kwanza.jeda.nio.server.http;

import org.glassfish.grizzly.http.Cookie;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpPacket;

import java.util.Collection;
import java.util.Map;

/**
 * @author Guzanov Alexander
 */
public interface IHttpRequest {

    public RequestID getID();

    public HttpContent getContent();

    public long getTimestamp();

    public String getRemoteAddress();

    public String getUri();

    public boolean finish(HttpPacket result);

    public Map<String, String> getHeaderMap();

    public String getHeader(String name);

    public Map<String, String> getAttributeMap();

    public String getAttribute(String name);

    public Map<String, String> getParameterMap();

    public String getParameter(String name);

    public Collection<Cookie> getCookies();

    public byte[] getBody();
}
