package ru.kwanza.jeda.api;

/**
 * @author Guzanov Alexander
 */
public class BusException extends Exception {
    public BusException() {
    }

    public BusException(String message) {
        super(message);
    }

    public BusException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusException(Throwable cause) {
        super(cause);
    }
}
