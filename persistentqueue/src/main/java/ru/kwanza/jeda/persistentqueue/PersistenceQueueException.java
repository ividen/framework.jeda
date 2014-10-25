package ru.kwanza.jeda.persistentqueue;

/**
 * @author Ivan Baluk
 */
public class PersistenceQueueException extends RuntimeException {

    public PersistenceQueueException(Exception cause) {
        super(cause);
    }
}
