package ru.kwanza.jeda.persistentqueue;

import ru.kwanza.jeda.clusterservice.Node;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public interface IQueuePersistenceController {

    String getQueueName();

    int getTotalCount(Node node);

    Collection<EventWithKey> load(int count, Node node);

    void delete(Collection<EventWithKey> result, Node node);

    void persist(Collection<EventWithKey> events, Node node);

    int transfer(int count, Node currentNode, Node repairableNode);
}
