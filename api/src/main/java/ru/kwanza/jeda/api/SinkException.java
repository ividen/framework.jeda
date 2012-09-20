package ru.kwanza.jeda.api;

/**
 * @author Guzanov Alexander
 */
public class SinkException extends Exception {
    public SinkException() {
    }

    public SinkException(String message) {
        super(message);
    }

    public SinkException(String message, Throwable cause) {
        super(message, cause);
    }

    public SinkException(Throwable cause) {
        super(cause);
    }

    public static final class Closed extends SinkException {
        public Closed(String message) {
            super(message);
        }

        public Closed(String message, Throwable cause) {
            super(message, cause);
        }

        public Closed(Throwable cause) {
            super(cause);
        }
    }

    public static final class Clogged extends SinkException {
        public Clogged(String message) {
            super(message);
        }
    }
}
