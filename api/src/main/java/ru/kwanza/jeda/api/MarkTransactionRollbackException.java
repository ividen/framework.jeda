package ru.kwanza.jeda.api;

/**
 * @author Guzanov Alexander
 */
public class MarkTransactionRollbackException extends RuntimeException {
    public MarkTransactionRollbackException() {
    }

    public MarkTransactionRollbackException(String message) {
        super(message);
    }

    public MarkTransactionRollbackException(String message, Throwable cause) {
        super(message, cause);
    }

    public MarkTransactionRollbackException(Throwable cause) {
        super(cause);
    }
}
