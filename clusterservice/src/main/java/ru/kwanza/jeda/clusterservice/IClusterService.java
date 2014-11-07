package ru.kwanza.jeda.clusterservice;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Alexander Guzanov
 */
public interface IClusterService {

    List<? extends Node> getActiveNodes();

    List<? extends Node> getPassiveNodes();

    List<? extends Node> getNodes();

    Node getCurrentNode();

    Map<String, IClusteredComponent> getComponents();

    Map<String, IClusteredComponent> getStartedComponents();

    Map<String, IClusteredComponent> getStoppedComponents();

    <R> R criticalSection(Callable<R> callable) throws InterruptedException, InvocationTargetException;

    <R> R criticalSection(Callable<R> callable, long waiteTimeout, TimeUnit unit) throws InterruptedException, InvocationTargetException, TimeoutException;

    void registerComponent(IClusteredComponent module);
}
