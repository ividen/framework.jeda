package ru.kwanza.jeda.clusterservice.impl.db;

/**
 * @author Alexander Guzanov
 */
public class ComponentInActiveExcetion extends Exception {
    public ComponentInActiveExcetion(String message) {
        super(message);
    }
}
