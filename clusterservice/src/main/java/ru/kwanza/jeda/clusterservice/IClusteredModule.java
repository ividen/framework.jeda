package ru.kwanza.jeda.clusterservice;

/**
 * @author Alexander Guzanov
 */
public interface IClusteredModule {
    String getName();

    void handleStart();

    void handleStop();

    boolean handleRepair(Node node);
}
