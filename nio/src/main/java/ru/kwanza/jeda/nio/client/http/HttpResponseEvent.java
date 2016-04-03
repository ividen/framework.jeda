package ru.kwanza.jeda.nio.client.http;

import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.util.HttpStatus;
import ru.kwanza.jeda.api.AbstractEvent;
import ru.kwanza.jeda.nio.client.ITransportEvent;

/**
 * @author Michael Yeskov
 */
public class HttpResponseEvent extends AbstractEvent {

    private ITransportEvent requestEvent;
    private HttpContent httpContent;
    private Throwable exception;
    private HttpStatus httpStatus;


    public HttpResponseEvent(ITransportEvent requestEvent, HttpContent httpContent, Throwable exception, HttpStatus httpStatus) {
        this.requestEvent = requestEvent;
        this.httpContent = httpContent;
        this.exception = exception;
        this.httpStatus = httpStatus;
    }

    public ITransportEvent getRequestEvent() {
        return requestEvent;
    }

    public HttpContent getHttpContent() {
        return httpContent;
    }

    public Throwable getException() {
        return exception;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public boolean hasException() {
        return exception != null;
    }
}
