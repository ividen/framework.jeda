package ru.kwanza.jeda.clusterservice;

import ru.kwanza.jeda.clusterservice.impl.db.orm.NodeEntity;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @author Alexander Guzanov
 */
public interface IClusterService {

    List<? extends NodeEntity> getActiveNodes();

    List<? extends Node> getPassiveNodes();

    List<? extends Node> getNodes();

    Node getCurrentNode();

    <R> R criticalSection(Callable<R> callable) throws InterruptedException, InvocationTargetException;

    <R> R criticalSection(Callable<R> callable,long waiteTimeout, TimeUnit unit);

    void registerModule(IClusteredModule module);
}
