package ru.kwanza.jeda.nio.server.http;

import ru.kwanza.jeda.api.IEvent;

/**
 * @author Guzanov Alexander
 */
public interface IHttpEvent extends IEvent {
    public IHttpRequest getHttpRequest();
}
