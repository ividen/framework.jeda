package ru.kwanza.jeda.clusterservice;

import ru.kwanza.jeda.clusterservice.impl.db.ComponentInActiveExcetion;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Сервис позволяющий определять
 *
 * @author Alexander Guzanov
 */
public interface IClusterService {

    List<? extends Node> getActiveNodes();

    List<? extends Node> getPassiveNodes();

    List<? extends Node> getNodes();

    Node getCurrentNode();

    Map<String, IClusteredComponent> getRepository();

    Map<String, IClusteredComponent> getActiveComponents();

    Map<String, IClusteredComponent> getPassiveComponents();

    <R> R criticalSection(IClusteredComponent component, Callable<R> callable)
            throws InvocationTargetException, ComponentInActiveExcetion;

    boolean markRepaired(IClusteredComponent component, Node node);

    void registerComponent(IClusteredComponent module);
}
