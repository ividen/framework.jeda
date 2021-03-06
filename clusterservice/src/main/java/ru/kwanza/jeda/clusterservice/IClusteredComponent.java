package ru.kwanza.jeda.clusterservice;

/**
 * @author Alexander Guzanov
 */
public interface IClusteredComponent {
    String getName();

    void handleStart();

    void handleStop();

    void handleStartRepair(Node node);

    void handleStopRepair(Node node);
}
