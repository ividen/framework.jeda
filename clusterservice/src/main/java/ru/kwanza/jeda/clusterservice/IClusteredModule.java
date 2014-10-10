package ru.kwanza.jeda.clusterservice;

/**
 * @author Alexander Guzanov
 */
public interface IClusteredModule {
    String getName();

    void handleConnected();

    void handleConnectionLost();

    boolean handleRepair(Node node);
}
