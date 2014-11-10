package ru.kwanza.jeda.clusterservice.impl.db;

/**
 * @author Alexander Guzanov
 */
public class ComponentInActiveExcetion extends Exception {
    public ComponentInActiveExcetion() {
    }

    public ComponentInActiveExcetion(String message) {
        super(message);
    }

    public ComponentInActiveExcetion(String message, Throwable cause) {
        super(message, cause);
    }

    public ComponentInActiveExcetion(Throwable cause) {
        super(cause);
    }
}
