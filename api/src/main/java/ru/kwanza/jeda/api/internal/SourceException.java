package ru.kwanza.jeda.api.internal;

/**
 * @author Guzanov Alexander
 */
public class SourceException extends Exception {
    public SourceException() {
    }

    public SourceException(String message) {
        super(message);
    }

    public SourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public SourceException(Throwable cause) {
        super(cause);
    }
}
