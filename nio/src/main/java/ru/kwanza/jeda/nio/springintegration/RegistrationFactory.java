package ru.kwanza.jeda.nio.springintegration;

import ru.kwanza.jeda.nio.server.http.EntryPoint;
import ru.kwanza.jeda.nio.server.http.HttpServer;
import ru.kwanza.jeda.nio.server.http.IHttpHandler;

import java.util.regex.Pattern;

/**
 * @author: Guzanov Alexander
 */
abstract class RegistrationFactory {

    public static Object registerEntryPoint(HttpServer server, EntryPoint entryPoint) {
        server.registerEntryPoint(entryPoint);
        return null;
    }

    public static Object registerHandlerByURI(HttpServer server, String uri, IHttpHandler handler) {
        server.registerHandler(uri, handler);
        return null;
    }

    public static Object registerHandlerByPattern(HttpServer server, String pattern, IHttpHandler handler) {
        server.registerHandler(Pattern.compile(pattern), handler);
        return null;
    }
}
