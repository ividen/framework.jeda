package ru.kwanza.jeda.clusterservice;

import ru.kwanza.jeda.clusterservice.impl.db.orm.NodeEntity;

import java.util.List;

/**
 * @author Alexander Guzanov
 */
public interface IClusterService {

    List<? extends NodeEntity> getActiveNodes();

    List<? extends Node> getPassiveNodes();

    List<? extends Node> getNodes();

    Node getCurrentNode();

    void registerModule(IClusteredModule module);
}
