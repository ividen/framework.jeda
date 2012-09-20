package ru.kwanza.jeda.nio.server.http;

/**
 * @author Guzanov Alexander
 */
public class RequestIDException extends Exception {
    public RequestIDException() {
    }

    public RequestIDException(String message) {
        super(message);
    }

    public RequestIDException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestIDException(Throwable cause) {
        super(cause);
    }
}
