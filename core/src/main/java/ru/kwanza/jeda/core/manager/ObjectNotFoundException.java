package ru.kwanza.jeda.core.manager;

/**
 * @author Guzanov Alexander
 */
public class ObjectNotFoundException extends RuntimeException {
    public ObjectNotFoundException(String message) {
        super(message);
    }
}
