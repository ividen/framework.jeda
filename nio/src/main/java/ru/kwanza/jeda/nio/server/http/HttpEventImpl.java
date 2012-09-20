package ru.kwanza.jeda.nio.server.http;

import ru.kwanza.jeda.api.AbstractEvent;

/**
 * @author Guzanov Alexander
 */
class HttpEventImpl extends AbstractEvent implements IHttpEvent {
    private IHttpRequest request;

    HttpEventImpl(IHttpRequest request) {
        this.request = request;
    }

    public IHttpRequest getHttpRequest() {
        return request;
    }
}
