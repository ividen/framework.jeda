package ru.kwanza.jeda.nio.server.http;

/**
 * @author Guzanov Alexander
 */
public interface ITimedOutHandler {

    public void onTimedOut(IHttpRequest request);
}
