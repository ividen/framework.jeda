package ru.kwanza.jeda.jeconnection;

/**
 * @author Guzanov Alexander
 */
public class JEConnectionException extends RuntimeException {
    public JEConnectionException() {
    }

    public JEConnectionException(String message) {
        super(message);
    }

    public JEConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public JEConnectionException(Throwable cause) {
        super(cause);
    }
}
