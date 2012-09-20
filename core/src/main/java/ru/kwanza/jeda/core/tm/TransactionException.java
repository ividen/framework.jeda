package ru.kwanza.jeda.core.tm;

/**
 * @author Guzanov Alexander
 */

public class TransactionException extends RuntimeException {
    public TransactionException(String message) {
        super(message);
    }

    public TransactionException(Throwable cause) {
        super(cause);
    }
}
