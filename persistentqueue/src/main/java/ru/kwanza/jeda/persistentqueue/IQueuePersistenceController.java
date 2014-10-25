package ru.kwanza.jeda.persistentqueue;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.clusterservice.Node;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public interface IQueuePersistenceController<E extends IPersistableEvent> {

    String getQueueName();

    int getTotalCount(Node node);

    Collection<E> load(int count, Node node);

    void delete(Collection<E> result, Node node);

    void persist(Collection<E> events, Node node);

    int transfer(int count, Node currentNode, Node repairableNode);
}
