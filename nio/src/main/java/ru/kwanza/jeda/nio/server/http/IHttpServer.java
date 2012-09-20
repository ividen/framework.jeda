package ru.kwanza.jeda.nio.server.http;

import java.util.regex.Pattern;

/**
 * @author Guzanov Alexander
 */
public interface IHttpServer {

    public String getName();

    public boolean registerHandler(String uri, IHttpHandler handler);

    public boolean registerHandler(Pattern pattern, IHttpHandler handler);
}
